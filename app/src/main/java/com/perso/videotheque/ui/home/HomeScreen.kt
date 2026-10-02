package com.perso.videotheque.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perso.videotheque.R
import com.perso.videotheque.domain.DurationRange
import com.perso.videotheque.domain.SortOrder
import com.perso.videotheque.ui.components.ConfirmDeleteDialog
import com.perso.videotheque.ui.components.TagChip
import com.perso.videotheque.ui.components.TagSelector
import com.perso.videotheque.ui.components.VideoGlyph
import com.perso.videotheque.util.openVideo

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddVideo: () -> Unit,
    onEditVideo: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    val isEmpty = !state.isLoading && state.totalCount == 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!isEmpty && !state.isLoading) {
                ExtendedFloatingActionButton(
                    onClick = onAddVideo,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Ajouter une vidéo") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp, 2.dp, 2.dp, 2.dp),
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize())
            isEmpty -> EmptyLibrary(onAddVideo, Modifier.padding(padding))
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                // Le contenu défile sous une barre d'état nette, sans la recouvrir.
                modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            ) {
                item { AppTitle() }
                item { SearchField(state.query.text, viewModel::onTextChange) }
                item { DurationFilter(state.query.duration, viewModel::onDurationChange) }
                item {
                    TagFilter(state, viewModel::onTagToggle, viewModel::onAllTags)
                }
                item {
                    ResultsBar(state.videos.size, state.query.sort, viewModel::onSortChange)
                }
                if (state.videos.isEmpty()) {
                    item { NoResults(viewModel::onResetFilters) }
                }
                items(state.videos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onOpen = { openVideo(context, video.url) },
                        onEdit = { onEditVideo(video.id) },
                        onDelete = { pendingDelete = video.id },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    pendingDelete?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { viewModel.delete(id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun AppTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
private fun SearchField(text: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Rechercher", maxLines = 1) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Effacer la recherche")
                }
            }
        },
        shape = MaterialTheme.shapes.medium,
        colors = quietFieldColors(),
    )
}

@Composable
fun quietFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationFilter(range: DurationRange, onChange: (Int, Int) -> Unit) {
    val min = DurationRange.MIN
    val max = DurationRange.MAX
    val summary = when {
        !range.isActive -> "Toutes"
        range.minMinutes == min -> "Jusqu'à ${minutesLabel(range.maxMinutes)}"
        range.maxMinutes == max -> "${minutesLabel(range.minMinutes)} et plus"
        else -> "${minutesLabel(range.minMinutes)} – ${minutesLabel(range.maxMinutes)}"
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("Durée", Modifier.weight(1f))
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        val colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        )
        // Piste fine et poignées rondes discrètes (au lieu du style Material par défaut, plus épais).
        RangeSlider(
            value = range.minMinutes.toFloat()..range.maxMinutes.toFloat(),
            onValueChange = { onChange(Math.round(it.start), Math.round(it.endInclusive)) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
            // Les poignées sont proches du bord : on évite que le geste "retour" d'Android les intercepte.
            modifier = Modifier.systemGestureExclusion(),
            colors = colors,
            startThumb = { SliderThumb("Durée minimale") },
            endThumb = { SliderThumb("Durée maximale") },
            track = { sliderState ->
                SliderDefaults.Track(
                    rangeSliderState = sliderState,
                    colors = colors,
                    modifier = Modifier.height(2.dp),
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 0.dp,
                    drawStopIndicator = null,
                )
            },
        )
        Row {
            Text(minutesLabel(min), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("${minutesLabel(max)} +", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 45 -> "45 min", 60 -> "1 h", 75 -> "1 h 15". */
private fun minutesLabel(minutes: Int) = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60} h"
    else -> "${minutes / 60} h %02d".format(minutes % 60)
}

@Composable
private fun TagFilter(state: HomeUiState, onToggle: (Long) -> Unit, onAll: () -> Unit) {
    Column {
        SectionLabel("Tags", Modifier.padding(bottom = 4.dp))
        TagSelector(
            tags = state.tags,
            selectedIds = state.query.tagIds,
            onToggle = onToggle,
            leading = { TagChip("Tous", state.query.tagIds.isEmpty(), onAll) },
        )
    }
}

@Composable
private fun ResultsBar(count: Int, sort: SortOrder, onSortChange: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (count <= 1) "$count vidéo" else "$count vidéos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(sort.label, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Changer le tri")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SortOrder.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        trailingIcon = {
                            if (option == sort) Icon(Icons.Filled.Check, contentDescription = "Sélectionné")
                        },
                        onClick = { expanded = false; onSortChange(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoResults(onReset: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
    ) {
        Text(
            "Aucune vidéo ne correspond",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onReset) { Text("Réinitialiser les filtres") }
    }
}

@Composable
private fun EmptyLibrary(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        AppTitle(Modifier.padding(top = 16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            VideoGlyph(size = 56.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Aucune vidéo enregistrée",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onAdd, shape = MaterialTheme.shapes.extraLarge) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.padding(start = 6.dp))
                Text("Ajouter une vidéo")
            }
        }
    }
}


@Composable
private fun SliderThumb(description: String) {
    Box(
        Modifier
            .size(22.dp)
            .semantics { contentDescription = description }
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
    )
}
