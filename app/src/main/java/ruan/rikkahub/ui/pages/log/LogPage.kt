package ruan.rikkahub.ui.pages.log

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import me.rerere.common.android.Logging
import ruan.rikkahub.ui.components.nav.BackButton

@Composable
fun LogPage() {
    var logs by remember { mutableStateOf(Logging.getRecentLogs()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(
                        onClick = {
                            Logging.clear()
                            logs = Logging.getRecentLogs()
                        }
                    ) {
                        Icon(Lucide.Trash2, null)
                    }
                }
            )
        }
    ) { contentPadding ->
        LogEntryList(
            logs = logs,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        )
    }
}

