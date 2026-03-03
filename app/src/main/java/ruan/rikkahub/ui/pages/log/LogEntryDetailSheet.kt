package ruan.rikkahub.ui.pages.log

import android.content.ClipData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.dokar.sonner.ToastType
import kotlinx.coroutines.launch
import me.rerere.common.android.LogEntry
import me.rerere.highlight.HighlightText
import ruan.rikkahub.R
import ruan.rikkahub.ui.context.LocalToaster
import ruan.rikkahub.ui.theme.JetbrainsMono
import ruan.rikkahub.utils.JsonInstantPretty
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun LogEntryDetailSheet(log: LogEntry) {
    val clipboard = LocalClipboard.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val copyText = remember(log) { log.toClipboardText() }
    val copiedMessage = stringResource(R.string.log_page_copied)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RowHeader(
            title = "Details",
            onCopy = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("log", copyText)))
                    toaster.show(
                        message = copiedMessage,
                        type = ToastType.Success,
                    )
                }
            },
        )
        when (log) {
            is LogEntry.RequestLog -> RequestLogDetail(log = log)
            is LogEntry.TextLog -> TextLogDetail(log = log)
        }
    }
}

@Composable
private fun RowHeader(title: String, onCopy: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Lucide.Copy,
                contentDescription = stringResource(R.string.log_page_copy),
            )
        }
    }
}

@Composable
private fun RequestLogDetail(log: LogEntry.RequestLog) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RequestLogSummary(log = log, dateFormat = dateFormat)
        HeadersSection(
            title = "Request Headers",
            headers = log.requestHeaders,
        )
        log.requestBody?.let { body ->
            BodySection(title = "Request Body", body = body)
        }
        HeadersSection(
            title = "Response Headers",
            headers = log.responseHeaders,
        )
    }
}

@Composable
private fun RequestLogSummary(log: LogEntry.RequestLog, dateFormat: SimpleDateFormat) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSection("Time", dateFormat.format(Date(log.timestamp)))
        DetailSection("Tag", log.tag)
        DetailSection("URL", log.url)
        DetailSection("Method", log.method)
        log.responseCode?.let { DetailSection("Status Code", it.toString()) }
        log.durationMs?.let { DetailSection("Duration", "${it}ms") }
        log.error?.let { DetailSection("Error", it) }
    }
}

@Composable
private fun HeadersSection(title: String, headers: Map<String, String>) {
    if (headers.isEmpty()) return

    HorizontalDivider()
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
    headers.forEach { (key, value) ->
        HeaderItem(key, value)
    }
}

@Composable
private fun BodySection(title: String, body: String) {
    HorizontalDivider()
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
    ) {
        HighlightText(
            code = JsonInstantPretty.encodeToString(JsonInstantPretty.parseToJsonElement(body)),
            language = "json",
            fontFamily = JetbrainsMono,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun TextLogDetail(log: LogEntry.TextLog) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailSection("Time", dateFormat.format(Date(log.timestamp)))
        DetailSection("Tag", log.tag)
        SelectionContainer {
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = JetbrainsMono,
            )
        }
    }
}

@Composable
private fun DetailSection(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = JetbrainsMono,
            )
        }
    }
}

@Composable
private fun HeaderItem(key: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = JetbrainsMono,
            )
        }
    }
}
