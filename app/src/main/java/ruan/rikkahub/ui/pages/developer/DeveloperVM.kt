package ruan.rikkahub.ui.pages.developer

import androidx.lifecycle.ViewModel
import ruan.rikkahub.data.ai.AILoggingManager

class DeveloperVM(
    private val aiLoggingManager: AILoggingManager
) : ViewModel() {
    val logs = aiLoggingManager.getLogs()
}
