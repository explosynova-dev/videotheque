package com.perso.videotheque.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.perso.videotheque.domain.DurationFormat
import com.perso.videotheque.ui.components.ConfirmDeleteDialog
import com.perso.videotheque.ui.components.HairlineBorder
import com.perso.videotheque.ui.components.TagSection
import com.perso.videotheque.ui.components.VideoGlyph
import com.perso.videotheque.ui.home.quietFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVideoScreen(
    viewModel: EditVideoViewModel,
    onClose: (saved: Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isDone) { if (state.isDone) onClose(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (state.isNew) "Ajouter une vidéo" else "Modifier la vidéo",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onClose(false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Supprimer la vidéo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            // Toujours visible, même clavier ouvert : le parcours depuis "Partager" reste en 1 clic.
            Box(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp)) {
                Button(
                    onClick = viewModel::save,
                    enabled = state.url.isNotBlank() && !state.isSaving,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(if (state.isSaving) "Enregistrement…" else "Enregistrer")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LinkField(state, viewModel::onUrlChange)
            Preview(state)
            TagSection(
                tags = state.allTags,
                selectedIds = state.selectedTagIds,
                onToggle = viewModel::onTagToggle,
                onCreate = viewModel::onCreateTag,
                onDelete = viewModel::onDeleteTag,
                alwaysShowNewTag = true,
            )
            DurationField(state, viewModel::onDurationChange)
        }
    }

    if (showDelete) {
        ConfirmDeleteDialog(
            onConfirm = { showDelete = false; viewModel.delete() },
            onDismiss = { showDelete = false },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun LinkField(state: EditUiState, onChange: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    Column {
        Label("Lien")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = state.url,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("https://www.youtube.com/watch?v=…") },
            isError = state.urlError != null,
            supportingText = state.urlError?.let { { Text(it) } },
            trailingIcon = {
                if (state.url.isEmpty()) {
                    TextButton(onClick = { clipboard.getText()?.text?.let(onChange) }) { Text("Coller") }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium,
            colors = quietFieldColors(),
        )
    }
}

@Composable
private fun Preview(state: EditUiState) {
    if (state.url.isBlank() || state.urlError != null) return
    Column {
        Surface(shape = MaterialTheme.shapes.large, border = HairlineBorder, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            // Aperçu compact à hauteur constante : les tags restent visibles sans défiler
            // et ne bougent pas quand les informations arrivent.
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(112.dp)
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    VideoGlyph(size = 28.dp)
                    if (state.thumbnailUrl != null) {
                        AsyncImage(
                            model = state.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    if (state.isFetching) {
                        LinearProgressIndicator(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp),
                            trackColor = Color.Transparent,
                        )
                    }
                }
                Column(Modifier.weight(1f).padding(start = 12.dp, end = 4.dp)) {
                    Text(
                        text = state.title ?: if (state.isFetching) "Récupération des informations…" else "Titre inconnu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.title != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            DurationFormat.parseInput(state.durationText)?.let(DurationFormat::format),
                            state.channelName,
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        state.info?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = if (state.isAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
            )
        }
    }
}

@Composable
private fun DurationField(state: EditUiState, onChange: (String) -> Unit) {
    Column {
        Label("Durée")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = state.durationText,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Automatique — ou min:s, ex. 18:45") },
            isError = state.durationError,
            supportingText = if (state.durationError) ({ Text("Format attendu : min:s (ex. 18:45)") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium,
            colors = quietFieldColors(),
        )
    }
}
