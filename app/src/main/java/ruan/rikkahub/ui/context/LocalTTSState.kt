package ruan.rikkahub.ui.context

import androidx.compose.runtime.compositionLocalOf
import ruan.rikkahub.ui.hooks.CustomTtsState

val LocalTTSState = compositionLocalOf<CustomTtsState> { error("Not provided yet") }
