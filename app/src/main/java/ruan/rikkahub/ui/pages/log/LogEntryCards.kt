package ruan.rikkahub.ui.pages.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.rerere.common.android.LogEntry
import ruan.rikkahub.ui.theme.JetbrainsMono
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun LogEntryCard(
    log: LogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (log) {
        is LogEntry.RequestLog -> RequestLogCard(log = log, onClick = onClick, modifier = modifier)
        is LogEntry.TextLog -> TextLogCard(log = log, onClick = onClick, modifier = modifier)
    }
}

@Composable
private fun RequestLogCard(
    log: LogEntry.RequestLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RequestLogHeaderRow(log = log, dateFormat = dateFormat)
            RequestLogUrlRow(url = log.url)
            RequestLogMetaRow(statusCode = log.responseCode, durationMs = log.durationMs)
            log.error?.let { RequestLogErrorRow(error = it) }
        }
    }
}

@Composable
private fun RequestLogHeaderRow(log: LogEntry.RequestLog, dateFormat: SimpleDateFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${log.tag} ${log.method}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = dateFormat.format(Date(log.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestLogUrlRow(url: String) {
    Text(
        text = url,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = JetbrainsMono,
        maxLines = 2,
    )
}

@Composable
private fun RequestLogMetaRow(statusCode: Int?, durationMs: Long?) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        statusCode?.let { code ->
            Text(
                text = "Status: $code",
                style = MaterialTheme.typography.labelSmall,
                color = if (code in 200..299) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
        durationMs?.let { duration ->
            Text(
                text = "${duration}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RequestLogErrorRow(error: String) {
    Text(
        text = "Error: $error",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TextLogCard(
    log: LogEntry.TextLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextLogHeaderRow(tag = log.tag, timestamp = log.timestamp, dateFormat = dateFormat)
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = JetbrainsMono,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TextLogHeaderRow(tag: String, timestamp: Long, dateFormat: SimpleDateFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = dateFormat.format(Date(timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

