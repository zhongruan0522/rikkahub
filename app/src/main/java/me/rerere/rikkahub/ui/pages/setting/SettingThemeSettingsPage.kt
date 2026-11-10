package me.rerere.rikkahub.ui.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.hooks.rememberColorMode
import me.rerere.rikkahub.ui.theme.ColorMode
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.findThemeById
import me.rerere.rikkahub.ui.theme.presets.BubblegumThemePreset
import me.rerere.rikkahub.ui.theme.presets.ClaudeThemePreset
import me.rerere.rikkahub.ui.theme.presets.DefaultThemePreset
import me.rerere.rikkahub.ui.theme.presets.MonoThemePreset
import me.rerere.rikkahub.ui.theme.presets.T3ChatThemePreset
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

// 与 Web UI 对齐的配色主题选项
private val ThemeOptions = listOf(
    DefaultThemePreset,
    ClaudeThemePreset,
    T3ChatThemePreset,
    MonoThemePreset,
    BubblegumThemePreset,
)

@Composable
fun SettingThemeSettingsPage(vm: SettingVM = koinViewModel()) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var colorMode by rememberColorMode()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(stringResource(R.string.setting_theme_settings_title))
                },
                navigationIcon = {
                    BackButton()
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CustomColors.topBarColors.containerColor
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item("themeSettings") {
                var themeMenuExpanded by remember { mutableStateOf(false) }
                var colorModeMenuExpanded by remember { mutableStateOf(false) }
                val currentTheme = findThemeById(settings.themeId, settings.customThemes)

                Box {
                    CardGroup {
                        item(
                            onClick = { themeMenuExpanded = true },
                            headlineContent = {
                                Text(stringResource(R.string.setting_theme_settings_theme))
                            },
                            supportingContent = {
                                currentTheme?.name()
                                    ?: Text(stringResource(R.string.setting_theme_settings_theme_missing))
                            },
                        )
                        item(
                            onClick = { colorModeMenuExpanded = true },
                            headlineContent = {
                                Text(stringResource(R.string.setting_theme_settings_color_mode))
                            },
                            supportingContent = {
                                Text(colorMode.displayName())
                            },
                        )
                    }

                    DropdownMenu(
                        expanded = themeMenuExpanded,
                        onDismissRequest = { themeMenuExpanded = false },
                    ) {
                        ThemeOptions.forEach { theme ->
                            DropdownMenuItem(
                                text = { theme.name() },
                                trailingIcon = if (settings.themeId == theme.id) {
                                    { Icon(HugeIcons.Tick01, contentDescription = null) }
                                } else {
                                    null
                                },
                                onClick = {
                                    vm.updateSettings { latest -> latest.copy(themeId = theme.id) }
                                    themeMenuExpanded = false
                                },
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = colorModeMenuExpanded,
                        onDismissRequest = { colorModeMenuExpanded = false },
                    ) {
                        ColorMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.displayName()) },
                                trailingIcon = if (colorMode == mode) {
                                    { Icon(HugeIcons.Tick01, contentDescription = null) }
                                } else {
                                    null
                                },
                                onClick = {
                                    colorMode = mode
                                    colorModeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorMode.displayName(): String = when (this) {
    ColorMode.SYSTEM -> stringResource(R.string.setting_page_color_mode_system)
    ColorMode.LIGHT -> stringResource(R.string.setting_page_color_mode_light)
    ColorMode.DARK -> stringResource(R.string.setting_page_color_mode_dark)
}
