package com.perso.videotheque.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perso.videotheque.data.repository.VideoRepository
import com.perso.videotheque.domain.DurationRange
import com.perso.videotheque.domain.SortOrder
import com.perso.videotheque.domain.Tag
import com.perso.videotheque.domain.Video
import com.perso.videotheque.domain.VideoQuery
import com.perso.videotheque.domain.applyQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalCount: Int = 0,
    val videos: List<Video> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val query: VideoQuery = VideoQuery(),
)

class HomeViewModel(private val repository: VideoRepository) : ViewModel() {

    private val query = MutableStateFlow(VideoQuery())

    // Le filtrage se fait en mémoire : instantané et largement suffisant pour une vidéothèque perso.
    val state: StateFlow<HomeUiState> =
        combine(repository.videos, repository.tags, query) { videos, tags, q ->
            HomeUiState(
                isLoading = false,
                totalCount = videos.size,
                videos = videos.applyQuery(q),
                tags = tags,
                query = q,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onTextChange(text: String) = query.update { it.copy(text = text) }

    fun onDurationChange(min: Int, max: Int) = query.update { it.copy(duration = DurationRange(min, max)) }

    fun onTagToggle(id: Long) = query.update {
        it.copy(tagIds = if (id in it.tagIds) it.tagIds - id else it.tagIds + id)
    }

    fun onAllTags() = query.update { it.copy(tagIds = emptySet()) }

    fun onSortChange(sort: SortOrder) = query.update { it.copy(sort = sort) }

    fun onResetFilters() = query.update { VideoQuery(sort = it.sort) }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
