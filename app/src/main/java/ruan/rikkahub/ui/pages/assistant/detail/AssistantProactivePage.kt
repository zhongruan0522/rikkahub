package ruan.rikkahub.ui.pages.assistant.detail

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ruan.rikkahub.R
import ruan.rikkahub.data.model.ProactiveConversationMode
import ruan.rikkahub.service.ProactiveMessageService
import ruan.rikkahub.ui.components.nav.BackButton
import ruan.rikkahub.ui.components.ui.FormItem
import ruan.rikkahub.ui.components.ui.OutlinedNumberInput
import ruan.rikkahub.ui.components.ui.permission.PermissionManager
import ruan.rikkahub.ui.components.ui.permission.PermissionNotification
import ruan.rikkahub.ui.components.ui.permission.rememberPermissionState

@Composable
fun AssistantProactivePage(id: String) {
    val vm: AssistantDetailVM = koinViewModel(
        parameters = { parametersOf(id) }
    )
    val assistant by vm.assistant.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionState = rememberPermissionState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setOf(
            PermissionNotification
        ) else emptySet(),
    )
    PermissionManager(permissionState = permissionState)

    fun syncServiceEnabled(enabled: Boolean) {
        val anyEnabledAfterUpdate = settings.assistants.any { a ->
            if (a.id == assistant.id) enabled else a.proactiveMessageConfig.enabled
        }
        if (anyEnabledAfterUpdate) {
            ProactiveMessageService.start(context.applicationContext)
        } else {
            ProactiveMessageService.stop(context.applicationContext)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.assistant_page_tab_proactive)) },
                navigationIcon = { BackButton() },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            ) {
                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_enable)) },
                    description = { Text(stringResource(R.string.assistant_proactive_enable_desc)) },
                    tail = {
                        Switch(
                            checked = assistant.proactiveMessageConfig.enabled,
                            onCheckedChange = { enabled ->
                                if (enabled && !permissionState.allRequiredPermissionsGranted) {
                                    permissionState.requestPermissions()
                                    return@Switch
                                }
                                val oldConfig = assistant.proactiveMessageConfig
                                vm.update(
                                    assistant.copy(
                                        proactiveMessageConfig = if (enabled) {
                                            oldConfig.copy(
                                                enabled = true,
                                                lastTriggeredAtEpochMillis = 0L,
                                            )
                                        } else {
                                            oldConfig.copy(enabled = false)
                                        }
                                    )
                                )
                                syncServiceEnabled(enabled)
                            }
                        )
                    }
                )

                HorizontalDivider()

                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_interval)) },
                    description = { Text(stringResource(R.string.assistant_proactive_interval_desc)) },
                ) {
                    OutlinedNumberInput(
                        value = assistant.proactiveMessageConfig.intervalMinutes,
                        onValueChange = { minutes ->
                            vm.update(
                                assistant.copy(
                                    proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                        intervalMinutes = minutes.coerceAtLeast(15)
                                    )
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.assistant_proactive_interval_input),
                    )
                }

                HorizontalDivider()

                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_conversation_mode)) },
                    description = { Text(stringResource(R.string.assistant_proactive_conversation_mode_desc)) },
                ) {
                    val selectedMode = assistant.proactiveMessageConfig.conversationMode
                    val options = listOf(
                        ProactiveConversationMode.USE_LATEST,
                        ProactiveConversationMode.NEW_CONVERSATION,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        options.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = mode == selectedMode,
                                onClick = {
                                    if (mode != selectedMode) {
                                        vm.update(
                                            assistant.copy(
                                                proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                                    conversationMode = mode,
                                                )
                                            )
                                        )
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            ) {
                                Text(
                                    text = stringResource(
                                        when (mode) {
                                            ProactiveConversationMode.USE_LATEST -> R.string.assistant_proactive_mode_latest
                                            ProactiveConversationMode.NEW_CONVERSATION -> R.string.assistant_proactive_mode_new
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            ) {
                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_prompt)) },
                    description = { Text(stringResource(R.string.assistant_proactive_prompt_desc)) },
                ) {
                    OutlinedTextField(
                        value = assistant.proactiveMessageConfig.prompt,
                        onValueChange = { text ->
                            vm.update(
                                assistant.copy(
                                    proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                        prompt = text
                                    )
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            ) {
                val quiet = assistant.proactiveMessageConfig.quietTime

                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_quiet_time)) },
                    description = { Text(stringResource(R.string.assistant_proactive_quiet_time_desc)) },
                    tail = {
                        Switch(
                            checked = quiet.enabled,
                            onCheckedChange = { enabled ->
                                vm.update(
                                    assistant.copy(
                                        proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                            quietTime = quiet.copy(enabled = enabled)
                                        )
                                    )
                                )
                            }
                        )
                    }
                )

                HorizontalDivider()

                FormItem(
                    modifier = Modifier.padding(8.dp),
                    label = { Text(stringResource(R.string.assistant_proactive_quiet_time_range)) },
                    description = { Text(stringResource(R.string.assistant_proactive_quiet_time_range_desc)) },
                ) {
                    var startText by remember(quiet.startMinuteOfDay) { mutableStateOf(minuteOfDayToString(quiet.startMinuteOfDay)) }
                    var endText by remember(quiet.endMinuteOfDay) { mutableStateOf(minuteOfDayToString(quiet.endMinuteOfDay)) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = startText,
                            onValueChange = { text ->
                                startText = text
                                parseMinuteOfDay(text)?.let { minute ->
                                    vm.update(
                                        assistant.copy(
                                            proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                                quietTime = quiet.copy(startMinuteOfDay = minute)
                                            )
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.assistant_proactive_quiet_time_start)) },
                            isError = parseMinuteOfDay(startText) == null,
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = endText,
                            onValueChange = { text ->
                                endText = text
                                parseMinuteOfDay(text)?.let { minute ->
                                    vm.update(
                                        assistant.copy(
                                            proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                                                quietTime = quiet.copy(endMinuteOfDay = minute)
                                            )
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.assistant_proactive_quiet_time_end)) },
                            isError = parseMinuteOfDay(endText) == null,
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }
}

private fun minuteOfDayToString(minuteOfDay: Int): String {
    val clamped = minuteOfDay.coerceIn(0, 1439)
    val hour = clamped / 60
    val minute = clamped % 60
    return "%02d%02d".format(hour, minute)
}

private fun parseMinuteOfDay(raw: String): Int? {
    val text = raw.trim()
    if (text.length != 4) return null
    val hour = text.substring(0, 2).toIntOrNull() ?: return null
    val minute = text.substring(2, 4).toIntOrNull() ?: return null
    if (hour !in 0..23) return null
    if (minute !in 0..59) return null
    return hour * 60 + minute
}
