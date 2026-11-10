package me.rerere.rikkahub.ui.pages.log

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.rerere.common.android.LogEntry
import me.rerere.common.android.Logging
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Copy01
import me.rerere.hugeicons.stroke.Delete01
import me.rerere.rikkahub.R
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.components.ui.JsonTree
import me.rerere.rikkahub.ui.components.ui.switchItem
import me.rerere.rikkahub.ui.context.LocalSettings
import me.rerere.rikkahub.ui.context.LocalToaster
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.ui.theme.JetbrainsMono
import me.rerere.rikkahub.utils.JsonInstantPretty
import me.rerere.ui.toast.ToastType
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogPage() {
    val settingsStore: SettingsStore = koinInject()
    val settings = LocalSettings.current
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf(Logging.getRecentLogs()) }
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Logs") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(
                        onClick = {
                            Logging.clear()
                            logs = Logging.getRecentLogs()
                        }
                    ) {
                        Icon(HugeIcons.Delete01, null)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CustomColors.topBarColors.containerColor,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                CardGroup {
                    switchItem(
                        checked = settings.requestLoggingEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                settingsStore.update { it.copy(requestLoggingEnabled = enabled) }
                            }
                        },
                        headlineContent = { Text(stringResource(R.string.log_page_record_requests)) },
                        supportingContent = {
                            Text(stringResource(R.string.log_page_record_requests_desc))
                        },
                    )
                    switchItem(
                        checked = settings.requestLoggingDetailed,
                        onCheckedChange = { detailed ->
                            scope.launch {
                                settingsStore.update { it.copy(requestLoggingDetailed = detailed) }
                            }
                        },
                        enabled = settings.requestLoggingEnabled,
                        headlineContent = { Text(stringResource(R.string.log_page_detailed_mode)) },
                        supportingContent = { Text(stringResource(R.string.log_page_detailed_mode_desc)) },
                    )
                }
            }

            val sortedLogs = remember(logs) { logs.sortedByDescending { it.timestamp } }
            items(sortedLogs, key = { it.id }, contentType = { it.javaClass.simpleName }) { log ->
                when (log) {
                    is LogEntry.RequestLog -> RequestLogCard(
                        log = log,
                        onClick = {
                            selectedLog = log
                            scope.launch { sheetState.show() }
                        },
                    )

                    is LogEntry.TextLog -> TextLogCard(
                        log = log,
                        onClick = {
                            selectedLog = log
                            scope.launch { sheetState.show() }
                        },
                    )
                }
            }
        }
    }

    selectedLog?.let { log ->
        ModalBottomSheet(
            onDismissRequest = { selectedLog = null },
            sheetState = sheetState,
        ) {
            when (log) {
                is LogEntry.RequestLog -> RequestLogDetail(log)
                is LogEntry.TextLog -> TextLogDetail(log)
            }
        }
    }
}

/** 复制文本到剪贴板，内容超出 Binder 限制时提示失败 */
@Composable
private fun rememberCopyHandler(): (String, String) -> Unit {
    val clipboard = LocalClipboard.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val copiedText = stringResource(R.string.copied)
    return { label, text ->
        scope.launch {
            try {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))
                toaster.show(copiedText)
            } catch (e: RuntimeException) {
                // 内容过大时 Binder 会抛出 TransactionTooLargeException (被包装为 RuntimeException)
                toaster.show("Copy failed: content too large for clipboard", ToastType.Error)
            }
        }
    }
}

/** 序列化整条日志，便于复制后反馈 */
private fun LogEntry.toCopyText(): String =
    JsonInstantPretty.encodeToString(LogEntry.serializer(), this)

/** 卡片右侧栏：时间戳 + 复制按钮（复制整条日志数据） */
@Composable
private fun LogCardMeta(
    log: LogEntry,
    dateFormat: SimpleDateFormat,
) {
    val copyHandler = rememberCopyHandler()
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = dateFormat.format(Date(log.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(
            onClick = { copyHandler(log.tag, log.toCopyText()) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = HugeIcons.Copy01,
                contentDescription = stringResource(R.string.copy),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RequestLogCard(
    log: LogEntry.RequestLog,
    onClick: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CustomColors.cardColorsOnSurfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = log.method,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = log.url,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = JetbrainsMono,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    log.responseCode?.let { code ->
                        Text(
                            text = "Status: $code",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (code in 200..299) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    log.durationMs?.let { duration ->
                        Text(
                            text = "${duration}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 错误摘要只显示简短信息，完整内容点开详情查看
                log.error?.let { error ->
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LogCardMeta(
                log = log,
                dateFormat = dateFormat,
            )
        }
    }
}

@Composable
private fun TextLogCard(
    log: LogEntry.TextLog,
    onClick: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CustomColors.cardColorsOnSurfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = JetbrainsMono,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            LogCardMeta(
                log = log,
                dateFormat = dateFormat,
            )
        }
    }
}

@Composable
private fun TextLogDetail(log: LogEntry.TextLog) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }

    SelectionContainer {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Log Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DetailSection("Time", dateFormat.format(Date(log.timestamp)))
            }

            item {
                DetailSection("Tag", log.tag)
            }

            item {
                DetailSection("Message", log.message)
            }

            log.detail?.let { detail ->
                item {
                    HorizontalDivider()
                    Text(
                        text = "Detail",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = JetbrainsMono,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestLogDetail(log: LogEntry.RequestLog) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }
    val copyHandler = rememberCopyHandler()

    SelectionContainer {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Request Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DetailSection("Time", dateFormat.format(Date(log.timestamp)))
            }

            item {
                DetailSection("URL", log.url)
            }

            item {
                DetailSection("Method", log.method)
            }

            log.responseCode?.let { code ->
                item {
                    DetailSection("Status Code", code.toString())
                }
            }

            log.durationMs?.let { duration ->
                item {
                    DetailSection("Duration", "${duration}ms")
                }
            }

            log.error?.let { error ->
                item {
                    DetailSection("Error", error)
                }
            }

            if (log.requestHeaders.isNotEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        text = "Request Headers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                log.requestHeaders.forEach { (key, value) ->
                    item {
                        HeaderItem(key, value)
                    }
                }
            }

            log.requestBody?.let { body ->
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Request Body",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        IconButton(
                            onClick = { copyHandler("Request Body", body) }
                        ) {
                            Icon(
                                imageVector = HugeIcons.Copy01,
                                contentDescription = stringResource(R.string.copy)
                            )
                        }
                    }
                    val jsonElement = remember(body) {
                        runCatching { JsonInstantPretty.parseToJsonElement(body) }.getOrNull()
                    }
                    if (jsonElement != null) {
                        JsonTree(
                            json = jsonElement,
                            modifier = Modifier.padding(top = 4.dp),
                            initialExpandLevel = 2
                        )
                    } else {
                        Text(
                            text = body,
                            fontFamily = JetbrainsMono,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (log.responseHeaders.isNotEmpty()) {
                item {
                    HorizontalDivider()
                    Text(
                        text = "Response Headers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                log.responseHeaders.forEach { (key, value) ->
                    item {
                        HeaderItem(key, value)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = JetbrainsMono
        )
    }
}

@Composable
private fun HeaderItem(key: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = JetbrainsMono
        )
    }
}
