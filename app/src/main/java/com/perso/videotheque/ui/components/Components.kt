package com.perso.videotheque.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.perso.videotheque.domain.Tag

/** Pastille de tag sélectionnable, très discrète. */
@Composable
fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
    )
}

/** Liste de tags qui passe automatiquement à la ligne. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    tags: List<Tag>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        leading()
        tags.forEach { tag -> TagChip(tag.name, tag.id in selectedIds) { onToggle(tag.id) } }
        trailing()
    }
}

/** Pictogramme vidéo (écran + play), même dessin que l'icône de l'application. */
@Composable
fun VideoGlyph(modifier: Modifier = Modifier, size: Dp = 48.dp, color: Color = MaterialTheme.colorScheme.outline) {
    // Coordonnées de l'icône (ic_launcher_foreground, zone 28..80 sur 108) ramenées à la taille demandée.
    Canvas(modifier.size(size)) {
        val u = this.size.width / 52f
        fun p(x: Float, y: Float) = Offset((x - 28f) * u, (y - 28f) * u)
        val stroke = Stroke(width = 2.6f * u, join = StrokeJoin.Round, cap = StrokeCap.Round)
        drawRoundRect(
            color = color,
            topLeft = p(32f, 36f),
            size = Size(44f * u, 28f * u),
            cornerRadius = CornerRadius(5f * u),
            style = stroke,
        )
        val play = Path().apply {
            p(50.5f, 44f).let { moveTo(it.x, it.y) }
            p(50.5f, 56f).let { lineTo(it.x, it.y) }
            p(60f, 50f).let { lineTo(it.x, it.y) }
            close()
        }
        drawPath(play, color, style = stroke)
        drawLine(color, p(45f, 71f), p(63f, 71f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
    }
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer cette vidéo ?", style = MaterialTheme.typography.titleMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
    )
}

@Composable
fun NewTagDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val submit = { if (name.isNotBlank()) onCreate(name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau tag", style = MaterialTheme.typography.titleMedium) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Nom du tag") },
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.focusRequester(focus),
            )
        },
        confirmButton = { TextButton(onClick = submit, enabled = name.isNotBlank()) { Text("Créer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
    )
    LaunchedEffect(Unit) { focus.requestFocus() }
}

val HairlineBorder @Composable get() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
