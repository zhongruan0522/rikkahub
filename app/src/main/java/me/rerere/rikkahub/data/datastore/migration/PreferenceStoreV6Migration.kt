package me.rerere.rikkahub.data.datastore.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.utils.JsonInstant
import me.rerere.rikkahub.utils.jsonPrimitiveOrNull

/**
 * ChatGPT/Grok 订阅提供商已移除，它们不是 ApiKey 的子类，留在设置里会让整份设置反序列化失败，
 * 读取前必须从提供商列表里删掉。
 */
class PreferenceStoreV6Migration : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val version = currentData[SettingsStore.VERSION]
        return version == null || version < 6
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val prefs = currentData.toMutablePreferences()

        prefs[SettingsStore.PROVIDERS]?.let { json ->
            prefs[SettingsStore.PROVIDERS] = migrateProvidersRemoveSubscription(json)
        }

        prefs[SettingsStore.VERSION] = 6
        return prefs.toPreferences()
    }

    override suspend fun cleanUp() {}
}

/** 从提供商列表 JSON 里移除 ChatGPT/Grok 订阅提供商，解析失败时原样返回 */
internal fun migrateProvidersRemoveSubscription(providersJson: String): String {
    return runCatching {
        val root = JsonInstant.parseToJsonElement(providersJson) as? JsonArray
            ?: return@runCatching providersJson
        JsonInstant.encodeToString(JsonArray(root.filterNot(::isSubscriptionProvider)))
    }.getOrDefault(providersJson)
}

// 模型级覆盖的提供商配置只会是 ApiKey 类型，只需要检查顶层条目
private fun isSubscriptionProvider(provider: JsonElement): Boolean {
    val obj = provider as? JsonObject ?: return false
    return obj["type"]?.jsonPrimitiveOrNull?.contentOrNull in SUBSCRIPTION_TYPES
}

private val SUBSCRIPTION_TYPES = setOf("chatgpt", "grok")
