package ruan.rikkahub.service

import android.app.PendingIntent
import android.app.Service
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessagePart
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ruan.rikkahub.PROACTIVE_MESSAGE_NOTIFICATION_CHANNEL_ID
import ruan.rikkahub.PROACTIVE_MESSAGE_TRIGGER_NOTIFICATION_CHANNEL_ID
import ruan.rikkahub.R
import ruan.rikkahub.RouteActivity
import ruan.rikkahub.data.datastore.SettingsStore
import ruan.rikkahub.data.model.Assistant
import ruan.rikkahub.data.model.ProactiveConversationMode
import ruan.rikkahub.data.model.ProactiveMessageConfig
import ruan.rikkahub.data.model.ProactiveQuietTime
import ruan.rikkahub.data.repository.ConversationRepository
import ruan.rikkahub.ui.hooks.writeStringPreference
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.math.max
import kotlin.uuid.Uuid
import java.util.concurrent.ConcurrentHashMap

private const val PROACTIVE_MESSAGE_NOTIFICATION_ID = 3
private const val PROACTIVE_TRIGGERED_NOTIFICATION_ID_BASE = 10_000
private const val MIN_INTERVAL_MINUTES = 15
private const val SCHEDULER_TICK_MILLIS = 30_000L
private const val PENDING_TRIGGER_TIMEOUT_MILLIS = 2 * 60 * 60_000L
private const val USER_IDLE_GRACE_MILLIS = 60_000L

class ProactiveMessageService : Service(), KoinComponent {
    companion object {
        private const val ACTION_START = "ruan.rikkahub.action.PROACTIVE_MESSAGE_START"
        private const val ACTION_STOP = "ruan.rikkahub.action.PROACTIVE_MESSAGE_STOP"

        fun start(context: Context) {
            val intent = Intent(context, ProactiveMessageService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ProactiveMessageService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val settingsStore: SettingsStore by inject()
    private val conversationRepo: ConversationRepository by inject()
    private val chatService: ChatService by inject()

    private val serviceJob = SupervisorJob()
    private val serviceScope: CoroutineScope = CoroutineScope(serviceJob + Dispatchers.Default)
    private val enabledAssistants = MutableStateFlow<List<Assistant>>(emptyList())
    private var schedulerJob: Job? = null
    private var delayedStopJob: Job? = null
    private val triggeringAssistantIds = ConcurrentHashMap.newKeySet<Uuid>()
    private val pendingTriggers = ConcurrentHashMap<Uuid, PendingTrigger>()
    private val lastUserMessageAtEpochMillis = MutableStateFlow(0L)
    private val lastUserConversationDoneAtEpochMillis = MutableStateFlow(0L)
    private val lastAppBackgroundAtEpochMillis = MutableStateFlow(0L)

    private data class PendingTrigger(
        val assistantId: Uuid,
        val assistantName: String,
        val prompt: String,
        val createdAtEpochMillis: Long,
    )

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            chatService.generationDoneFlow.collect { conversationId ->
                val pending = pendingTriggers.remove(conversationId) ?: return@collect
                onProactiveReplyDone(conversationId = conversationId, pending = pending)
            }
        }
        serviceScope.launch {
            settingsStore.lastUserMessageAtFlow.collectLatest { epochMillis ->
                lastUserMessageAtEpochMillis.value = epochMillis
            }
        }
        serviceScope.launch {
            settingsStore.lastUserConversationDoneAtFlow.collectLatest { epochMillis ->
                lastUserConversationDoneAtEpochMillis.value = epochMillis
            }
        }
        serviceScope.launch {
            settingsStore.lastAppBackgroundAtFlow.collectLatest { epochMillis ->
                lastAppBackgroundAtEpochMillis.value = epochMillis
            }
        }
        serviceScope.launch {
            settingsStore.settingsFlow
                .map { settings -> settings.assistants.filter { it.proactiveMessageConfig.enabled } }
                .collectLatest { assistants ->
                    enabledAssistants.value = assistants
                    updateForegroundNotification(activeCount = assistants.size)
                    if (assistants.isEmpty()) {
                        delayedStopJob?.cancel()
                        delayedStopJob = serviceScope.launch {
                            delay(3000)
                            if (enabledAssistants.value.isEmpty()) {
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                            }
                        }
                    } else {
                        delayedStopJob?.cancel()
                        delayedStopJob = null
                    }
                }
        }
        schedulerJob = serviceScope.launch {
            schedulerLoop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START, null -> {
                if (!canPostNotifications()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForeground(PROACTIVE_MESSAGE_NOTIFICATION_ID, buildNotification(activeCount = enabledAssistants.value.size))
                return START_STICKY
            }

            else -> return START_STICKY
        }
    }

    override fun onDestroy() {
        schedulerJob?.cancel()
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun schedulerLoop() {
        while (serviceJob.isActive) {
            cleanupExpiredPendingTriggers()

            val assistants = enabledAssistants.value
            if (assistants.isEmpty()) {
                delay(1000)
                continue
            }

            val now = ZonedDateTime.now()
            val nowEpoch = now.toInstant().toEpochMilli()
            val userIdleStartEpochMillis = computeUserIdleStartEpochMillis(nowEpochMillis = nowEpoch)

            assistants.forEach { assistant ->
                val config = assistant.proactiveMessageConfig
                val nextAt = computeNextTriggerAtEpochMillis(
                    now = now,
                    config = config,
                    userIdleStartEpochMillis = userIdleStartEpochMillis,
                )
                if (nowEpoch >= nextAt) {
                    if (triggeringAssistantIds.add(assistant.id)) {
                        serviceScope.launch {
                            try {
                                triggerAssistant(now = now, assistant = assistant, config = config)
                            } finally {
                                triggeringAssistantIds.remove(assistant.id)
                            }
                        }
                    }
                }
            }

            delay(SCHEDULER_TICK_MILLIS)
        }
    }

    private suspend fun triggerAssistant(
        now: ZonedDateTime,
        assistant: Assistant,
        config: ProactiveMessageConfig,
    ) {
        runCatching {
            if (config.prompt.isBlank()) {
                updateLastTriggeredAt(assistantId = assistant.id, epochMillis = now.toInstant().toEpochMilli())
                return@runCatching
            }

            val conversationId = when (config.conversationMode) {
                ProactiveConversationMode.NEW_CONVERSATION -> Uuid.random()
                ProactiveConversationMode.USE_LATEST ->
                    conversationRepo.getLatestConversationIdOfAssistant(assistantId = assistant.id) ?: Uuid.random()
            }

            if (chatService.isGenerating(conversationId)) {
                return@runCatching
            }

            val nowEpoch = now.toInstant().toEpochMilli()
            updateLastTriggeredAt(assistantId = assistant.id, epochMillis = nowEpoch)

            chatService.initializeConversationForAssistant(
                conversationId = conversationId,
                assistantId = assistant.id,
                setAsCurrentAssistant = false,
            )

            val assistantName = assistant.name.ifBlank { getString(R.string.assistant_page_default_assistant) }
            pendingTriggers[conversationId] = PendingTrigger(
                assistantId = assistant.id,
                assistantName = assistantName,
                prompt = config.prompt,
                createdAtEpochMillis = nowEpoch,
            )

            chatService.sendMessage(
                conversationId = conversationId,
                content = listOf(UIMessagePart.Text(config.prompt)),
                answer = true,
                recordUserActivity = false,
            )
        }
    }

    private fun cleanupExpiredPendingTriggers() {
        if (pendingTriggers.isEmpty()) return
        val now = System.currentTimeMillis()
        pendingTriggers.entries.removeIf { (_, pending) ->
            now - pending.createdAtEpochMillis > PENDING_TRIGGER_TIMEOUT_MILLIS
        }
    }

    private fun onProactiveReplyDone(conversationId: Uuid, pending: PendingTrigger) {
        // 让用户点通知时直接落在对应会话上（同时也兼容“关闭后下次打开回到上次会话”的场景）
        writeStringPreference("lastConversationId", conversationId.toString())

        sendTriggeredNotification(
            assistantName = pending.assistantName,
            conversationId = conversationId,
            messagePreview = buildAssistantReplyPreview(conversationId = conversationId) ?: pending.prompt,
        )

        if (chatService.isForeground.value) {
            openConversationInApp(conversationId = conversationId)
        }
    }

    private fun buildAssistantReplyPreview(conversationId: Uuid): String? {
        val conversation = chatService.getConversationFlow(conversationId).value
        val lastAssistantMessage = conversation.currentMessages.lastOrNull { it.role == MessageRole.ASSISTANT }
            ?: return null
        return lastAssistantMessage.toText().trim().take(400)
    }

    private fun openConversationInApp(conversationId: Uuid) {
        val intent = Intent(this, RouteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("conversationId", conversationId.toString())
        }
        startActivity(intent)
    }

    private suspend fun updateLastTriggeredAt(assistantId: Uuid, epochMillis: Long) {
        settingsStore.update { settings ->
            settings.copy(
                assistants = settings.assistants.map { assistant ->
                    if (assistant.id != assistantId) return@map assistant
                    assistant.copy(
                        proactiveMessageConfig = assistant.proactiveMessageConfig.copy(
                            lastTriggeredAtEpochMillis = epochMillis
                        )
                    )
                }
            )
        }
    }

    private fun computeUserIdleStartEpochMillis(nowEpochMillis: Long): Long? {
        val lastConversationDoneAt = lastUserConversationDoneAtEpochMillis.value
        val lastBackgroundAt = lastAppBackgroundAtEpochMillis.value
        val baseAt = max(lastConversationDoneAt, lastBackgroundAt)
        if (baseAt <= 0L) return null

        val lastUserMessageAt = lastUserMessageAtEpochMillis.value
        if (lastUserMessageAt > baseAt) return null

        val idleStart = baseAt + USER_IDLE_GRACE_MILLIS
        if (nowEpochMillis < idleStart) return null

        return idleStart
    }

    private fun computeNextTriggerAtEpochMillis(
        now: ZonedDateTime,
        config: ProactiveMessageConfig,
        userIdleStartEpochMillis: Long?,
    ): Long {
        val idleStart = userIdleStartEpochMillis ?: return Long.MAX_VALUE

        val intervalMinutes = config.intervalMinutes.coerceAtLeast(MIN_INTERVAL_MINUTES)
        val intervalMillis = intervalMinutes * 60_000L
        val nowEpoch = now.toInstant().toEpochMilli()

        val notBeforeEpoch = idleStart + intervalMillis
        val base = if (config.lastTriggeredAtEpochMillis <= 0L) {
            notBeforeEpoch
        } else {
            max(config.lastTriggeredAtEpochMillis + intervalMillis, notBeforeEpoch)
        }

        val candidateEpoch = max(base, nowEpoch)
        val candidate = Instant.ofEpochMilli(candidateEpoch).atZone(now.zone)

        return adjustForQuietTime(candidate = candidate, quietTime = config.quietTime)
    }

    private fun adjustForQuietTime(candidate: ZonedDateTime, quietTime: ProactiveQuietTime): Long {
        if (!quietTime.enabled) return candidate.toInstant().toEpochMilli()

        val start = quietTime.startMinuteOfDay.coerceIn(0, 1439)
        val end = quietTime.endMinuteOfDay.coerceIn(0, 1439)
        if (start == end) return candidate.toInstant().toEpochMilli()

        val minutes = candidate.toLocalTime().minuteOfDay()
        val inQuiet = if (start < end) {
            minutes in start until end
        } else {
            minutes >= start || minutes < end
        }
        if (!inQuiet) return candidate.toInstant().toEpochMilli()

        val endHour = end / 60
        val endMinute = end % 60
        val endToday = candidate.toLocalDate().atTime(endHour, endMinute).atZone(candidate.zone)
        val endTime = if (start < end) {
            endToday
        } else {
            if (minutes >= start) endToday.plusDays(1) else endToday
        }

        return endTime.toInstant().toEpochMilli()
    }

    private fun LocalTime.minuteOfDay(): Int = hour * 60 + minute

    private fun updateForegroundNotification(activeCount: Int) {
        if (!canPostNotifications()) return
        NotificationManagerCompat.from(this).notify(
            PROACTIVE_MESSAGE_NOTIFICATION_ID,
            buildNotification(activeCount = activeCount)
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildNotification(activeCount: Int) =
        NotificationCompat.Builder(this, PROACTIVE_MESSAGE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(getString(R.string.notification_proactive_message_title))
            .setContentText(getString(R.string.notification_proactive_message_desc, activeCount))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent())
            .addAction(
                0,
                getString(R.string.notification_proactive_message_action_stop),
                stopPendingIntent()
            )
            .build()

    private fun sendTriggeredNotification(assistantName: String, conversationId: Uuid, messagePreview: String) {
        if (!canPostNotifications()) return
        val notification = NotificationCompat.Builder(this, PROACTIVE_MESSAGE_TRIGGER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(getString(R.string.notification_proactive_message_triggered_title, assistantName))
            .setContentText(getString(R.string.notification_proactive_message_triggered_desc))
            .setStyle(NotificationCompat.BigTextStyle().bigText(messagePreview.take(800)))
            .setAutoCancel(true)
            .setContentIntent(openConversationPendingIntent(conversationId))
            .build()
        NotificationManagerCompat.from(this).notify(triggeredNotificationId(conversationId), notification)
    }

    private fun triggeredNotificationId(conversationId: Uuid): Int {
        return PROACTIVE_TRIGGERED_NOTIFICATION_ID_BASE + conversationId.hashCode()
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(this, RouteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun openConversationPendingIntent(conversationId: Uuid): PendingIntent {
        val intent = Intent(this, RouteActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("conversationId", conversationId.toString())
        }
        return PendingIntent.getActivity(
            this,
            conversationId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(this, ProactiveMessageService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
