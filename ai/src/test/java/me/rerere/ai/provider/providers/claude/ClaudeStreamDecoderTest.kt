package me.rerere.ai.provider.providers.claude

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.ai.provider.stream.SseEvent
import me.rerere.ai.ui.ClaudeReasoningMetadata
import me.rerere.ai.ui.StreamChunk
import me.rerere.ai.util.json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 回归测试: ClaudeStreamDecoder 的 signature_delta 累加 (第 1b 点)
 * 与 redacted_thinking 保留 (第 1c 点).
 */
class ClaudeStreamDecoderTest {

    private fun sse(event: String, data: String) = SseEvent(event = event, data = data)

    @Test
    fun `signature_delta should accumulate across multiple deltas`() {
        val decoder = ClaudeStreamDecoder()

        // content_block_start: thinking block with no initial signature
        decoder.accept(sse("content_block_start", json.encodeToString(buildJsonObject {
            put("type", "content_block_start")
            put("index", 0)
            put("content_block", buildJsonObject {
                put("type", "thinking")
                put("thinking", "")
            })
        })))

        // thinking_delta: some text
        decoder.accept(sse("content_block_delta", json.encodeToString(buildJsonObject {
            put("type", "content_block_delta")
            put("index", 0)
            put("delta", buildJsonObject {
                put("type", "thinking_delta")
                put("thinking", "Let me think")
            })
        })))

        // signature_delta: first fragment
        decoder.accept(sse("content_block_delta", json.encodeToString(buildJsonObject {
            put("type", "content_block_delta")
            put("index", 0)
            put("delta", buildJsonObject {
                put("type", "signature_delta")
                put("signature", "frag_1_")
            })
        })))

        // signature_delta: second fragment
        decoder.accept(sse("content_block_delta", json.encodeToString(buildJsonObject {
            put("type", "content_block_delta")
            put("index", 0)
            put("delta", buildJsonObject {
                put("type", "signature_delta")
                put("signature", "frag_2")
            })
        })))

        // content_block_stop
        val stopResult = decoder.accept(sse("content_block_stop", json.encodeToString(buildJsonObject {
            put("type", "content_block_stop")
            put("index", 0)
        })))

        // Find the ReasoningEnd chunk and verify accumulated signature
        val reasoningEnd = stopResult.chunks.filterIsInstance<StreamChunk.ReasoningEnd>().firstOrNull()
        assertNotNull("Should have ReasoningEnd", reasoningEnd)
        val metadata = reasoningEnd!!.metadata
        assertNotNull("Should have metadata", metadata)
        val reasoningMeta = metadata!!.let {
            runCatching { json.decodeFromJsonElement<ClaudeReasoningMetadata>(it) }.getOrNull()
        }
        assertNotNull("Should have ClaudeReasoningMetadata", reasoningMeta)
        assertEquals("frag_1_frag_2", reasoningMeta!!.signature)
    }

    @Test
    fun `redacted_thinking should preserve data in metadata`() {
        val decoder = ClaudeStreamDecoder()

        val startResult = decoder.accept(sse("content_block_start", json.encodeToString(buildJsonObject {
            put("type", "content_block_start")
            put("index", 0)
            put("content_block", buildJsonObject {
                put("type", "redacted_thinking")
                put("data", "encrypted_redacted_data")
            })
        })))

        val reasoningStart = startResult.chunks.filterIsInstance<StreamChunk.ReasoningStart>().firstOrNull()
        assertNotNull("Should have ReasoningStart", reasoningStart)

        val stopResult = decoder.accept(sse("content_block_stop", json.encodeToString(buildJsonObject {
            put("type", "content_block_stop")
            put("index", 0)
        })))

        val reasoningEnd = stopResult.chunks.filterIsInstance<StreamChunk.ReasoningEnd>().firstOrNull()
        assertNotNull("Should have ReasoningEnd", reasoningEnd)
        val metadata = reasoningEnd!!.metadata
        assertNotNull("Should have metadata with redacted data", metadata)
        val reasoningMeta = metadata!!.let {
            runCatching { json.decodeFromJsonElement<ClaudeReasoningMetadata>(it) }.getOrNull()
        }
        assertNotNull("Should have ClaudeReasoningMetadata", reasoningMeta)
        assertEquals("encrypted_redacted_data", reasoningMeta!!.redactedData)
    }

    @Test
    fun `single signature_delta should work without prior signature`() {
        val decoder = ClaudeStreamDecoder()

        decoder.accept(sse("content_block_start", json.encodeToString(buildJsonObject {
            put("type", "content_block_start")
            put("index", 0)
            put("content_block", buildJsonObject {
                put("type", "thinking")
                put("thinking", "")
            })
        })))

        val result = decoder.accept(sse("content_block_delta", json.encodeToString(buildJsonObject {
            put("type", "content_block_delta")
            put("index", 0)
            put("delta", buildJsonObject {
                put("type", "signature_delta")
                put("signature", "full_signature_abc")
            })
        })))

        val delta = result.chunks.filterIsInstance<StreamChunk.ReasoningDelta>().firstOrNull()
        assertNotNull("Should have ReasoningDelta", delta)
        val reasoningMeta = delta!!.metadata!!.let {
            runCatching { json.decodeFromJsonElement<ClaudeReasoningMetadata>(it) }.getOrNull()
        }
        assertEquals("full_signature_abc", reasoningMeta!!.signature)
    }
}
