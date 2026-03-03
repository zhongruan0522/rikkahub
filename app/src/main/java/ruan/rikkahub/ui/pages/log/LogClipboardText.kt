package ruan.rikkahub.ui.pages.log

import me.rerere.common.android.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun LogEntry.toClipboardText(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    return when (this) {
        is LogEntry.RequestLog -> buildRequestLogText(dateFormat)
        is LogEntry.TextLog -> buildTextLogText(dateFormat)
    }
}

private fun LogEntry.RequestLog.buildRequestLogText(dateFormat: SimpleDateFormat): String {
    return buildString {
        appendLine("type=request")
        appendLine("time=${dateFormat.format(Date(timestamp))}")
        appendLine("tag=$tag")
        appendLine("method=$method")
        appendLine("url=$url")
        responseCode?.let { appendLine("status=$it") }
        durationMs?.let { appendLine("durationMs=$it") }
        error?.let { appendLine("error=$it") }
        if (requestHeaders.isNotEmpty()) {
            appendLine()
            appendLine("[requestHeaders]")
            requestHeaders.forEach { (k, v) -> appendLine("$k: $v") }
        }
        requestBody?.let { body ->
            appendLine()
            appendLine("[requestBody]")
            appendLine(body)
        }
        if (responseHeaders.isNotEmpty()) {
            appendLine()
            appendLine("[responseHeaders]")
            responseHeaders.forEach { (k, v) -> appendLine("$k: $v") }
        }
    }.trimEnd()
}

private fun LogEntry.TextLog.buildTextLogText(dateFormat: SimpleDateFormat): String {
    return buildString {
        appendLine("type=text")
        appendLine("time=${dateFormat.format(Date(timestamp))}")
        appendLine("tag=$tag")
        appendLine()
        appendLine(message)
    }.trimEnd()
}

