package com.perso.videotheque.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perso.videotheque.data.metadata.MetadataFetcher
import com.perso.videotheque.data.repository.VideoRepository
import com.perso.videotheque.domain.BodyKeywords
import com.perso.videotheque.domain.DurationFormat
import com.perso.videotheque.domain.Tag
import com.perso.videotheque.domain.Video
import com.perso.videotheque.domain.VideoLinks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class EditUiState(
    val isNew: Boolean = true,
    val url: String = "",
    val urlError: String? = null,
    val isFetching: Boolean = false,
    val title: String? = null,
    val channelName: String? = null,
    val thumbnailUrl: String? = null,
    val durationText: String = "",
    val durationError: Boolean = false,
    val isAvailable: Boolean = true,
    /** Message discret sous l'aperçu (infos manquantes, vidéo supprimée…). */
    val info: String? = null,
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
)

class EditVideoViewModel(
    private val repository: VideoRepository,
    private val fetcher: MetadataFetcher,
    private val videoId: Long?,
    sharedText: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(EditUiState(isNew = videoId == null))
    val state: StateFlow<EditUiState> = _state.asStateFlow()

    private var existing: Video? = null
    private var fetchedUrl: String? = null
    private var fetchJob: Job? = null

    init {
        viewModelScope.launch { repository.tags.collect { tags -> _state.update { it.copy(allTags = tags) } } }
        when {
            videoId != null -> viewModelScope.launch { load(videoId) }
            sharedText != null -> {
                _state.update { it.copy(url = VideoLinks.extractUrl(sharedText) ?: sharedText.trim()) }
                fetchJob = viewModelScope.launch { resolveUrl() }
            }
        }
    }

    private suspend fun load(id: Long) {
        val video = repository.getVideo(id) ?: return
        existing = video
        fetchedUrl = video.url
        _state.update {
            it.copy(
                url = video.url,
                title = video.title,
                channelName = video.channelName,
                thumbnailUrl = video.thumbnailUrl,
                durationText = video.durationSeconds?.let(DurationFormat::toInput).orEmpty(),
                isAvailable = video.isAvailable,
                info = if (video.isAvailable) null else UNAVAILABLE,
                selectedTagIds = video.tags.mapTo(HashSet()) { it.id },
            )
        }
        // Infos incomplètes (ex. ajoutée hors connexion) : on retente la récupération.
        if (video.title == null || video.durationSeconds == null) {
            fetchedUrl = null
            fetchJob = viewModelScope.launch { resolveUrl() }
        }
    }

    fun onUrlChange(text: String) {
        // Un texte partagé/collé peut contenir autre chose que le lien.
        val url = if (text.contains(' ')) VideoLinks.extractUrl(text) ?: text else text
        _state.update { it.copy(url = url, urlError = null) }
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            delay(400)
            resolveUrl()
        }
    }

    /** Valide le lien puis récupère titre, miniature et durée. N'empêche jamais l'enregistrement. */
    private suspend fun resolveUrl() {
        val raw = _state.value.url.trim()
        if (raw.isEmpty()) return
        if (!VideoLinks.isValidUrl(raw)) {
            _state.update { it.copy(urlError = INVALID_LINK) }
            return
        }
        val url = VideoLinks.normalize(raw)
        if (url == fetchedUrl) return

        val youTubeId = VideoLinks.youTubeId(url)
        _state.update {
            it.copy(
                isFetching = youTubeId != null,
                info = null,
                title = null,
                channelName = null,
                thumbnailUrl = youTubeId?.let(VideoLinks::youTubeThumbnail),
            )
        }
        val meta = if (youTubeId != null) fetcher.fetch(url) else null
        fetchedUrl = url
        // Zones du corps reconnues dans le titre (FR/EN) : tags présélectionnés, toujours décochables.
        val keywordTagIds = BodyKeywords.detect(meta?.title.orEmpty()).map { repository.createTag(it).id }
        _state.update { s ->
            s.copy(
                isFetching = false,
                selectedTagIds = s.selectedTagIds + keywordTagIds,
                title = meta?.title,
                channelName = meta?.channelName,
                thumbnailUrl = meta?.thumbnailUrl ?: s.thumbnailUrl,
                durationText = meta?.durationSeconds?.let(DurationFormat::toInput) ?: s.durationText,
                durationError = false,
                isAvailable = meta?.isAvailable ?: true,
                info = when {
                    youTubeId == null -> "Lien non YouTube : il sera enregistré tel quel."
                    meta == null -> "Informations indisponibles (pas de connexion ?). La vidéo peut quand même être enregistrée."
                    !meta.isAvailable -> UNAVAILABLE
                    meta.durationSeconds == null -> "Durée introuvable : tu peux la saisir ci-dessous."
                    else -> null
                },
            )
        }
    }

    fun onDurationChange(text: String) {
        val clean = text.filter { it.isDigit() || it == ':' }
        _state.update { it.copy(durationText = clean, durationError = clean.isNotBlank() && DurationFormat.parseInput(clean) == null) }
    }

    fun onTagToggle(id: Long) = _state.update {
        it.copy(selectedTagIds = if (id in it.selectedTagIds) it.selectedTagIds - id else it.selectedTagIds + id)
    }

    fun onCreateTag(name: String) {
        viewModelScope.launch {
            val tag = repository.createTag(name)
            _state.update { it.copy(selectedTagIds = it.selectedTagIds + tag.id) }
        }
    }

    fun save() {
        val raw = _state.value.url.trim()
        if (!VideoLinks.isValidUrl(raw)) {
            _state.update { it.copy(urlError = INVALID_LINK) }
            return
        }
        if (_state.value.durationError || _state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            // Si la récupération est en cours, on lui laisse un court délai, sans bloquer l'enregistrement.
            val pending = fetchJob
            if (pending?.isActive == true) withTimeoutOrNull(FETCH_WAIT_MS) { pending.join() }
            if (VideoLinks.normalize(raw) != fetchedUrl) withTimeoutOrNull(FETCH_WAIT_MS) { resolveUrl() }

            val s = _state.value
            val now = System.currentTimeMillis()
            repository.save(
                Video(
                    id = existing?.id ?: 0,
                    url = VideoLinks.normalize(raw),
                    title = s.title,
                    thumbnailUrl = s.thumbnailUrl,
                    channelName = s.channelName,
                    durationSeconds = DurationFormat.parseInput(s.durationText),
                    isAvailable = s.isAvailable,
                    tags = s.allTags.filter { it.id in s.selectedTagIds },
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
            _state.update { it.copy(isSaving = false, isDone = true) }
        }
    }

    fun delete() {
        val id = existing?.id ?: return
        viewModelScope.launch {
            repository.delete(id)
            _state.update { it.copy(isDone = true) }
        }
    }

    private companion object {
        const val INVALID_LINK = "Ce lien ne semble pas être valide."
        const val UNAVAILABLE = "Cette vidéo semble supprimée ou privée : ses informations ne sont plus accessibles."
        const val FETCH_WAIT_MS = 8_000L
    }
}
