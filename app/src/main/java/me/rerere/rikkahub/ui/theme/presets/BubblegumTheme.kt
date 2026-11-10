package me.rerere.rikkahub.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.theme.PresetTheme

/**
 * Bubblegum 泡泡糖风格（与 Web UI bubblegum 配色一致）：粉色主色，
 * 青色与奶油黄撞色点缀，蓝灰暗色底，糖果感撞色。
 */
val BubblegumThemePreset by lazy {
    PresetTheme(
        id = "bubblegum",
        name = {
            Text(stringResource(id = R.string.theme_name_bubblegum))
        },
        standardLight = lightScheme,
        standardDark = darkScheme,
    )
}

private val primaryLight = Color(0xFFD04F99)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFFD3EA)
private val onPrimaryContainerLight = Color(0xFFA2266F)
private val secondaryLight = Color(0xFF4E9CA3)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFB2E1EB)
private val onSecondaryContainerLight = Color(0xFF1F4F55)
private val tertiaryLight = Color(0xFFB2912B)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFBE2A7)
private val onTertiaryContainerLight = Color(0xFF6B5410)
private val errorLight = Color(0xFFF96F70)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD5)
private val onErrorContainerLight = Color(0xFF9C2016)
private val backgroundLight = Color(0xFFF6E6EE)
private val onBackgroundLight = Color(0xFF4A4A4A)
private val surfaceLight = Color(0xFFF6E6EE)
private val onSurfaceLight = Color(0xFF4A4A4A)
private val surfaceVariantLight = Color(0xFFF1DCE8)
private val onSurfaceVariantLight = Color(0xFF7A7A7A)
private val outlineLight = Color(0xFFD5A2C2)
private val outlineVariantLight = Color(0xFFEFD3E4)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF31222B)
private val inverseOnSurfaceLight = Color(0xFFF9EDF4)
private val inversePrimaryLight = Color(0xFFE8A9CD)
private val surfaceDimLight = Color(0xFFE7CFDD)
private val surfaceBrightLight = Color(0xFFFFF7FB)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFAF0F6)
private val surfaceContainerLight = Color(0xFFF4E4EE)
private val surfaceContainerHighLight = Color(0xFFEFDAE8)
private val surfaceContainerHighestLight = Color(0xFFE7CFDD)

private val primaryDark = Color(0xFFFBE2A7)
private val onPrimaryDark = Color(0xFF33290F)
private val primaryContainerDark = Color(0xFF665416)
private val onPrimaryContainerDark = Color(0xFFFFEBBD)
private val secondaryDark = Color(0xFFE4A2B1)
private val onSecondaryDark = Color(0xFF44222D)
private val secondaryContainerDark = Color(0xFF5D3B47)
private val onSecondaryContainerDark = Color(0xFFFFD9E1)
private val tertiaryDark = Color(0xFFA9D4DA)
private val onTertiaryDark = Color(0xFF1F3D42)
private val tertiaryContainerDark = Color(0xFF2F555B)
private val onTertiaryContainerDark = Color(0xFFC6ECF1)
private val errorDark = Color(0xFFE35EA4)
private val onErrorDark = Color(0xFF56093A)
private val errorContainerDark = Color(0xFF7A104F)
private val onErrorContainerDark = Color(0xFFFFD9EC)
private val backgroundDark = Color(0xFF12242E)
private val onBackgroundDark = Color(0xFFF3E3EA)
private val surfaceDark = Color(0xFF12242E)
private val onSurfaceDark = Color(0xFFF3E3EA)
private val surfaceVariantDark = Color(0xFF1F303A)
private val onSurfaceVariantDark = Color(0xFFC4BFC7)
private val outlineDark = Color(0xFF94A0AA)
private val outlineVariantDark = Color(0xFF324859)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF3E3EA)
private val inverseOnSurfaceDark = Color(0xFF26333C)
private val inversePrimaryDark = Color(0xFFD04F99)
private val surfaceDimDark = Color(0xFF0E1D26)
private val surfaceBrightDark = Color(0xFF3A4C56)
private val surfaceContainerLowestDark = Color(0xFF0C1A22)
private val surfaceContainerLowDark = Color(0xFF1A2A34)
private val surfaceContainerDark = Color(0xFF1F303A)
private val surfaceContainerHighDark = Color(0xFF273742)
private val surfaceContainerHighestDark = Color(0xFF324859)

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)
