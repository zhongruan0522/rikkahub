package me.rerere.ai.provider.providers.claude

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.rerere.ai.core.MessageRole
import me.rerere.ai.core.ReasoningLevel
import me.rerere.ai.provider.Model
import me.rerere.ai.provider.ModelAbility
import me.rerere.ai.provider.ProviderSetting
import me.rerere.ai.provider.TextGenerationParams
import me.rerere.ai.ui.ClaudeReasoningMetadata
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.ai.ui.toMetadata
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 回归测试: Claude thinking 参数按模型世代映射、采样参数门控、max_tokens clamp、
 * drop_block 兜底、redacted_thinking 回传 (对应 docs/claude-400-errors.md 第 1~5 点).
 */
class ClaudeThinkingParamsTest {

    private lateinit var provider: ClaudeProvider
    private val claudeSetting = ProviderSetting.Claude(apiKey = "test-key")

    private val legacyModel = Model(
        modelId = "claude-sonnet-4-5",
        abilities = listOf(ModelAbility.REASONING),
    )
    private val newModel = Model(
        modelId = "claude-opus-4-6",
        abilities = listOf(ModelAbility.REASONING),
    )
    private val oldModel = Model(
        modelId = "claude-3-5-sonnet",
        abilities = listOf(ModelAbility.REASONING),
    )

    @Before
    fun setUp() {
        provider = ClaudeProvider(OkHttpClient())
    }

    private fun buildRequest(
        model: Model,
        reasoningLevel: ReasoningLevel,
        maxTokens: Int? = null,
        temperature: Float? = null,
        topP: Float? = null,
    ): JsonObject {
        val params = TextGenerationParams(
            model = model,
            reasoningLevel = reasoningLevel,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
        )
        val messages = listOf(UIMessage.user("hi"))
        val method = ClaudeProvider::class.java.getDeclaredMethod(
            "buildMessageRequest",
            ProviderSetting.Claude::class.java,
            List::class.java,
            TextGenerationParams::class.java,
            Boolean::class.javaPrimitiveType,
        )
        method.isAccessible = true
        return method.invoke(provider, claudeSetting, messages, params, false) as JsonObject
    }

    // ==================== Point 2: thinking by model generation ====================

    @Test
    fun `new generation AUTO should use adaptive thinking`() {
        val req = buildRequest(newModel, ReasoningLevel.AUTO)
        val thinking = req["thinking"]!!.jsonObject
        assertEquals("adaptive", thinking["type"]!!.jsonPrimitive.content)
        assertEquals("summarized", thinking["display"]!!.jsonPrimitive.content)
        // AUTO should not send output_config.effort
        assertNull(req["output_config"])
    }

    @Test
    fun `new generation HIGH should use adaptive with output_config effort`() {
        val req = buildRequest(newModel, ReasoningLevel.HIGH)
        val thinking = req["thinking"]!!.jsonObject
        assertEquals("adaptive", thinking["type"]!!.jsonPrimitive.content)
        assertEquals("high", req["output_config"]!!.jsonObject["effort"]!!.jsonPrimitive.content)
    }

    @Test
    fun `legacy generation AUTO should use enabled with budget_tokens`() {
        val req = buildRequest(legacyModel, ReasoningLevel.AUTO)
        val thinking = req["thinking"]!!.jsonObject
        assertEquals("enabled", thinking["type"]!!.jsonPrimitive.content)
        val budget = thinking["budget_tokens"]!!.jsonPrimitive.intOrNull!!
        assertTrue("budget should be >= 1024", budget >= 1024)
        assertNull(req["output_config"])
    }

    @Test
    fun `legacy generation HIGH should use enabled with budget_tokens from level`() {
        val req = buildRequest(legacyModel, ReasoningLevel.HIGH, maxTokens = 64_000)
        val thinking = req["thinking"]!!.jsonObject
        assertEquals("enabled", thinking["type"]!!.jsonPrimitive.content)
        assertEquals(8000, thinking["budget_tokens"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun `legacy budget_tokens should be clamped to max_tokens minus one`() {
        val req = buildRequest(legacyModel, ReasoningLevel.MAX, maxTokens = 2000)
        val thinking = req["thinking"]!!.jsonObject
        val budget = thinking["budget_tokens"]!!.jsonPrimitive.intOrNull!!
        assertEquals(1999, budget)
    }

    @Test
    fun `OFF should omit thinking field entirely`() {
        val req = buildRequest(newModel, ReasoningLevel.OFF)
        assertNull(req["thinking"])
        // Also for legacy
        val req2 = buildRequest(legacyModel, ReasoningLevel.OFF)
        assertNull(req2["thinking"])
    }

    // ==================== Point 1d: drop_block fallback ====================

    @Test
    fun `thinking enabled should include drop_block prefix_mismatch_behavior`() {
        val req = buildRequest(newModel, ReasoningLevel.AUTO)
        val thinking = req["thinking"]!!.jsonObject
        val blockBinding = thinking["block_binding"]!!.jsonObject
        assertEquals("drop_block", blockBinding["prefix_mismatch_behavior"]!!.jsonPrimitive.content)
    }

    @Test
    fun `legacy thinking enabled should also include drop_block`() {
        val req = buildRequest(legacyModel, ReasoningLevel.MEDIUM)
        val thinking = req["thinking"]!!.jsonObject
        val blockBinding = thinking["block_binding"]!!.jsonObject
        assertEquals("drop_block", blockBinding["prefix_mismatch_behavior"]!!.jsonPrimitive.content)
    }

    @Test
    fun `thinking disabled should not include drop_block`() {
        val req = buildRequest(newModel, ReasoningLevel.OFF)
        assertNull(req["thinking"])
    }

    // ==================== Point 3: sampling params gating ====================

    @Test
    fun `new generation should not send temperature even when thinking is off`() {
        val req = buildRequest(newModel, ReasoningLevel.OFF, temperature = 0.7f, topP = 0.9f)
        assertNull(req["temperature"])
        assertNull(req["top_p"])
    }

    @Test
    fun `legacy generation with thinking off should send temperature and top_p`() {
        val req = buildRequest(legacyModel, ReasoningLevel.OFF, temperature = 0.7f, topP = 0.9f)
        assertEquals(0.7f, req["temperature"]!!.jsonPrimitive.content.toFloat())
        assertEquals(0.9f, req["top_p"]!!.jsonPrimitive.content.toFloat())
    }

    @Test
    fun `legacy generation with thinking on should not send temperature or top_p`() {
        val req = buildRequest(legacyModel, ReasoningLevel.HIGH, temperature = 0.7f, topP = 0.9f)
        assertNull(req["temperature"])
        assertNull(req["top_p"])
    }

    @Test
    fun `old claude-3-5 model should send temperature when thinking off`() {
        val req = buildRequest(oldModel, ReasoningLevel.OFF, temperature = 0.5f)
        assertEquals(0.5f, req["temperature"]!!.jsonPrimitive.content.toFloat())
    }

    // ==================== Point 4: max_tokens clamp ====================

    @Test
    fun `max_tokens should be clamped to claude-3-5 limit of 8192`() {
        val req = buildRequest(oldModel, ReasoningLevel.OFF, maxTokens = 64_000)
        assertEquals(8192, req["max_tokens"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun `max_tokens should be clamped to claude-3 limit of 4096`() {
        val claude3Model = Model(modelId = "claude-3-opus", abilities = listOf(ModelAbility.REASONING))
        val req = buildRequest(claude3Model, ReasoningLevel.OFF, maxTokens = 64_000)
        assertEquals(4096, req["max_tokens"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun `max_tokens below limit should not be raised`() {
        val req = buildRequest(oldModel, ReasoningLevel.OFF, maxTokens = 4000)
        assertEquals(4000, req["max_tokens"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun `max_tokens default should be clamped for old models`() {
        val req = buildRequest(oldModel, ReasoningLevel.OFF)
        assertEquals(8192, req["max_tokens"]!!.jsonPrimitive.intOrNull)
    }

    @Test
    fun `max_tokens for new models should remain at 64000 when not set`() {
        val req = buildRequest(newModel, ReasoningLevel.OFF)
        assertEquals(64_000, req["max_tokens"]!!.jsonPrimitive.intOrNull)
    }

    // ==================== Point 1c: redacted_thinking round-trip ====================

    @Test
    fun `redacted_thinking should be sent back as redacted_thinking block`() {
        val messages = listOf(
            UIMessage.user("hi"),
            UIMessage(
                role = MessageRole.ASSISTANT,
                parts = listOf(
                    UIMessagePart.Reasoning(
                        reasoning = "",
                        metadata = ClaudeReasoningMetadata(redactedData = "encrypted_blob_123").toMetadata(),
                    ),
                    UIMessagePart.Text("response"),
                ),
            ),
        )
        val params = TextGenerationParams(model = newModel, reasoningLevel = ReasoningLevel.OFF)
        val method = ClaudeProvider::class.java.getDeclaredMethod(
            "buildMessageRequest",
            ProviderSetting.Claude::class.java,
            List::class.java,
            TextGenerationParams::class.java,
            Boolean::class.javaPrimitiveType,
        )
        method.isAccessible = true
        val req = method.invoke(provider, claudeSetting, messages, params, false) as JsonObject

        val assistantContent = req["messages"]!!.jsonArray
            .firstOrNull { it.jsonObject["role"]?.jsonPrimitive?.content == "assistant" }
            ?.jsonObject?.get("content")?.jsonArray
            ?: error("No assistant message found")

        val redactedBlock = assistantContent.firstOrNull {
            it.jsonObject["type"]?.jsonPrimitive?.content == "redacted_thinking"
        }
        assertTrue("Should have redacted_thinking block", redactedBlock != null)
        assertEquals("encrypted_blob_123", redactedBlock!!.jsonObject["data"]!!.jsonPrimitive.content)
    }
}
