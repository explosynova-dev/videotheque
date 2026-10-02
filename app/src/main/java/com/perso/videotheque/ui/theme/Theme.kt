package com.perso.videotheque.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Palette gris-bleu clair + un seul accent discret (bleu ardoise).
val OffWhite = Color(0xFFEDF1F4)
val White = Color(0xFFFFFFFF)
val CardWhite = Color(0xFFF8FAFB)
val LightGrey = Color(0xFFE0E7EC)
val Hairline = Color(0xFFD2DBE2)
val MidGrey = Color(0xFFAAB8C3)
val TextGrey = Color(0xFF566673)
val SoftBlack = Color(0xFF1D2933)
val Accent = Color(0xFF46657F)
val AccentSoft = Color(0xFFD8E3EB)

private val colors = lightColorScheme(
    primary = Accent,
    onPrimary = White,
    primaryContainer = AccentSoft,
    onPrimaryContainer = Accent,
    secondary = TextGrey,
    onSecondary = White,
    secondaryContainer = LightGrey,
    onSecondaryContainer = SoftBlack,
    background = OffWhite,
    onBackground = SoftBlack,
    surface = OffWhite,
    onSurface = SoftBlack,
    surfaceVariant = LightGrey,
    onSurfaceVariant = TextGrey,
    surfaceContainerLowest = CardWhite,
    surfaceContainerLow = CardWhite,
    surfaceContainer = CardWhite,
    surfaceContainerHigh = CardWhite,
    surfaceContainerHighest = LightGrey,
    outline = MidGrey,
    outlineVariant = Hairline,
    error = Color(0xFFA2463B),
)

private val base = Typography()
private val typography = base.copy(
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.2).sp),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Medium),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun VideothequeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes, content = content)
}
