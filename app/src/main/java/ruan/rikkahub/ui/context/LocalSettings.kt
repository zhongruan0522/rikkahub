package ruan.rikkahub.ui.context

import androidx.compose.runtime.staticCompositionLocalOf
import ruan.rikkahub.data.datastore.Settings

val LocalSettings = staticCompositionLocalOf<Settings> {
    error("No SettingsStore provided")
}
