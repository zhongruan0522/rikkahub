package me.rerere.rikkahub.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.theme.PresetTheme

/**
 * T3 Chat 风格（与 Web UI t3-chat 配色一致）：淡粉底色，
 * 玫红主色，紫红点缀，柔和粉紫层级。
 */
val T3ChatThemePreset by lazy {
    PresetTheme(
        id = "t3-chat",
        name = {
            Text(stringResource(id = R.string.theme_name_t3chat))
        },
        standardLight = lightScheme,
        standardDark = darkScheme,
    )
}

private val primaryLight = Color(0xFFA84370)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFAD7E9)
private val onPrimaryContainerLight = Color(0xFF6E1E4E)
private val secondaryLight = Color(0xFF77347C)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFF1C4E6)
private val onSecondaryContainerLight = Color(0xFF77347C)
private val tertiaryLight = Color(0xFF834588)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFF2DFF4)
private val onTertiaryContainerLight = Color(0xFF5E2563)
private val errorLight = Color(0xFFAB4347)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF8C2B2F)
private val backgroundLight = Color(0xFFFAF5FA)
private val onBackgroundLight = Color(0xFF501854)
private val surfaceLight = Color(0xFFFAF5FA)
private val onSurfaceLight = Color(0xFF501854)
private val surfaceVariantLight = Color(0xFFF6E5F3)
private val onSurfaceVariantLight = Color(0xFF834588)
private val outlineLight = Color(0xFFB77CB3)
private val outlineVariantLight = Color(0xFFEFBDEB)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF362D3D)
private val inverseOnSurfaceLight = Color(0xFFF6E5F3)
private val inversePrimaryLight = Color(0xFFFFB1D2)
private val surfaceDimLight = Color(0xFFEED9EA)
private val surfaceBrightLight = Color(0xFFFFFBFF)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFAF3F9)
private val surfaceContainerLight = Color(0xFFF4E9F3)
private val surfaceContainerHighLight = Color(0xFFEFDCED)
private val surfaceContainerHighestLight = Color(0xFFEED9EA)

private val primaryDark = Color(0xFFFFB1D2)
private val onPrimaryDark = Color(0xFF5E1038)
private val primaryContainerDark = Color(0xFF7D1140)
private val onPrimaryContainerDark = Color(0xFFFFD9E6)
private val secondaryDark = Color(0xFFE1B6DC)
private val onSecondaryDark = Color(0xFF3F1E43)
private val secondaryContainerDark = Color(0xFF362D3D)
private val onSecondaryContainerDark = Color(0xFFD4C7E1)
private val tertiaryDark = Color(0xFFD9B8DE)
private val onTertiaryDark = Color(0xFF3B1F42)
private val tertiaryContainerDark = Color(0xFF543459)
private val onTertiaryContainerDark = Color(0xFFF2DFF4)
private val errorDark = Color(0xFFFFB1B6)
private val onErrorDark = Color(0xFF5E1122)
private val errorContainerDark = Color(0xFF7E2033)
private val onErrorContainerDark = Color(0xFFFFD9DC)
private val backgroundDark = Color(0xFF221D27)
private val onBackgroundDark = Color(0xFFE8DCE9)
private val surfaceDark = Color(0xFF221D27)
private val onSurfaceDark = Color(0xFFE8DCE9)
private val surfaceVariantDark = Color(0xFF2A2430)
private val onSurfaceVariantDark = Color(0xFFC2B6CF)
private val outlineDark = Color(0xFF9B8FA8)
private val outlineVariantDark = Color(0xFF3B3237)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF4E9F3)
private val inverseOnSurfaceDark = Color(0xFF2A2430)
private val inversePrimaryDark = Color(0xFFA84370)
private val surfaceDimDark = Color(0xFF1B161F)
private val surfaceBrightDark = Color(0xFF3F3844)
private val surfaceContainerLowestDark = Color(0xFF1B161F)
private val surfaceContainerLowDark = Color(0xFF262029)
private val surfaceContainerDark = Color(0xFF2A2430)
private val surfaceContainerHighDark = Color(0xFF342E38)
private val surfaceContainerHighestDark = Color(0xFF3F3844)

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
