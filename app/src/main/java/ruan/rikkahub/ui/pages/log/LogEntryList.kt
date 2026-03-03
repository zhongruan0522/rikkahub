package ruan.rikkahub.ui.pages.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.rerere.common.android.LogEntry

@Composable
internal fun LogEntryList(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier,
) {
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val sortedLogs = remember(logs) { logs.sortedByDescending { it.timestamp } }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(sortedLogs, key = { it.id }, contentType = { it.javaClass.simpleName }) { log ->
            LogEntryCard(
                log = log,
                onClick = {
                    selectedLog = log
                    scope.launch { sheetState.show() }
                },
            )
        }
    }

    selectedLog?.let { log ->
        ModalBottomSheet(
            onDismissRequest = { selectedLog = null },
            sheetState = sheetState,
        ) {
            LogEntryDetailSheet(log = log)
        }
    }
}

