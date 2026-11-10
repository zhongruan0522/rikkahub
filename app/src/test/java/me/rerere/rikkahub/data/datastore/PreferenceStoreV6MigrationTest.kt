package me.rerere.rikkahub.data.datastore

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import me.rerere.ai.provider.ProviderSetting
import me.rerere.rikkahub.data.datastore.migration.SettingsJsonMigrator
import me.rerere.rikkahub.data.datastore.migration.migrateProvidersRemoveSubscription
import me.rerere.rikkahub.utils.JsonInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceStoreV6MigrationTest {
    private fun migrate(json: String): List<ProviderSetting> =
        JsonInstant.decodeFromString<List<ProviderSetting>>(migrateProvidersRemoveSubscription(json))

    @Test
    fun `subscription providers are dropped and api key providers kept`() {
        val providers = migrate(
            """[
                {"type":"chatgpt","name":"ChatGPT","models":[]},
                {"type":"openai","name":"DeepSeek","baseUrl":"https://api.deepseek.com/v1","apiKey":"sk-test","models":[]},
                {"type":"grok","name":"Grok","models":[]},
                {"type":"claude","name":"Claude","baseUrl":"https://api.anthropic.com/v1","apiKey":"sk-ant","models":[]}
            ]"""
        )

        assertTrue(providers.all { it is ProviderSetting.OpenAI || it is ProviderSetting.Claude })
    }

    @Test
    fun `migrated providers deserialize without unknown types`() {
        // 反序列化失败正是迁移要解决的问题：ChatGPT/Grok 不再是 ProviderSetting 的子类
        val raw = """[{"type":"chatgpt","name":"ChatGPT","models":[]}]"""

        assertEquals(emptyList<ProviderSetting>(), migrate(raw))
    }

    @Test
    fun `migration can be applied again without changing the result`() {
        val once = migrateProvidersRemoveSubscription("""[{"type":"grok","name":"Grok","models":[]}]""")

        assertEquals(once, migrateProvidersRemoveSubscription(once))
    }

    @Test
    fun `unparseable providers are left untouched`() {
        assertEquals("not json", migrateProvidersRemoveSubscription("not json"))
    }

    @Test
    fun `backup settings json drops subscription providers`() {
        val migrated = SettingsJsonMigrator.migrate(
            """{"providers":[{"type":"chatgpt","name":"ChatGPT","models":[]},{"type":"google","name":"Google","models":[]}]}"""
        )

        val providers = JsonInstant.parseToJsonElement(migrated).jsonObject["providers"] as JsonArray
        assertEquals(1, providers.jsonArray.size)
        assertEquals("google", providers.single().jsonObject["type"]?.toString()?.trim('"'))
    }
}
