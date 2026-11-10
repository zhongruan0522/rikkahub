package me.rerere.rikkahub.ui.theme.presets

import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.theme.PresetTheme

/**
 * Mono 极简单色风格（与 Web UI mono 配色一致）：纯灰度配色，
 * 中灰主色，无彩色层级，红色错误色点缀。
 */
val MonoThemePreset by lazy {
    PresetTheme(
        id = "mono",
        name = {
            Text(stringResource(id = R.string.theme_name_mono))
        },
        standardLight = lightScheme,
        standardDark = darkScheme,
    )
}

private val primaryLight = Color(0xFF737373)
private val onPrimaryLight = Color(0xFFFAFAFA)
private val primaryContainerLight = Color(0xFFE5E5E5)
private val onPrimaryContainerLight = Color(0xFF404040)
private val secondaryLight = Color(0xFF595959)
private val onSecondaryLight = Color(0xFFFAFAFA)
private val secondaryContainerLight = Color(0xFFF5F5F5)
private val onSecondaryContainerLight = Color(0xFF171717)
private val tertiaryLight = Color(0xFF595959)
private val onTertiaryLight = Color(0xFFFAFAFA)
private val tertiaryContainerLight = Color(0xFFEBEBEB)
private val onTertiaryContainerLight = Color(0xFF333333)
private val errorLight = Color(0xFFE7000B)
private val onErrorLight = Color(0xFFF5F5F5)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFFFFF)
private val onBackgroundLight = Color(0xFF0A0A0A)
private val surfaceLight = Color(0xFFFFFFFF)
private val onSurfaceLight = Color(0xFF0A0A0A)
private val surfaceVariantLight = Color(0xFFF5F5F5)
private val onSurfaceVariantLight = Color(0xFF717171)
private val outlineLight = Color(0xFFA1A1A1)
private val outlineVariantLight = Color(0xFFE5E5E5)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF262626)
private val inverseOnSurfaceLight = Color(0xFFF5F5F5)
private val inversePrimaryLight = Color(0xFFD4D4D4)
private val surfaceDimLight = Color(0xFFE5E5E5)
private val surfaceBrightLight = Color(0xFFFFFFFF)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFAFAFA)
private val surfaceContainerLight = Color(0xFFF5F5F5)
private val surfaceContainerHighLight = Color(0xFFF0F0F0)
private val surfaceContainerHighestLight = Color(0xFFE5E5E5)

private val primaryDark = Color(0xFFA3A3A3)
private val onPrimaryDark = Color(0xFF1F1F1F)
private val primaryContainerDark = Color(0xFF333333)
private val onPrimaryContainerDark = Color(0xFFE5E5E5)
private val secondaryDark = Color(0xFFA1A1A1)
private val onSecondaryDark = Color(0xFF1F1F1F)
private val secondaryContainerDark = Color(0xFF262626)
private val onSecondaryContainerDark = Color(0xFFFAFAFA)
private val tertiaryDark = Color(0xFFA1A1A1)
private val onTertiaryDark = Color(0xFF1F1F1F)
private val tertiaryContainerDark = Color(0xFF333333)
private val onTertiaryContainerDark = Color(0xFFE5E5E5)
private val errorDark = Color(0xFFFF6467)
private val onErrorDark = Color(0xFF262626)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF0A0A0A)
private val onBackgroundDark = Color(0xFFFAFAFA)
private val surfaceDark = Color(0xFF0A0A0A)
private val onSurfaceDark = Color(0xFFFAFAFA)
private val surfaceVariantDark = Color(0xFF262626)
private val onSurfaceVariantDark = Color(0xFFA1A1A1)
private val outlineDark = Color(0xFF737373)
private val outlineVariantDark = Color(0xFF383838)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFFAFAFA)
private val inverseOnSurfaceDark = Color(0xFF262626)
private val inversePrimaryDark = Color(0xFF595959)
private val surfaceDimDark = Color(0xFF0A0A0A)
private val surfaceBrightDark = Color(0xFF343434)
private val surfaceContainerLowestDark = Color(0xFF050505)
private val surfaceContainerLowDark = Color(0xFF141414)
private val surfaceContainerDark = Color(0xFF1A1A1A)
private val surfaceContainerHighDark = Color(0xFF232323)
private val surfaceContainerHighestDark = Color(0xFF2E2E2E)

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
