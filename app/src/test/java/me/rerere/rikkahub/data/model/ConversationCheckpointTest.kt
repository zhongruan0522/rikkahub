package me.rerere.rikkahub.data.model

import me.rerere.ai.core.MessageRole
import me.rerere.ai.core.TokenUsage
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.limitContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class ConversationCheckpointTest {
    @Test
    fun `context checkpoint is inserted after the anchor node without touching other nodes`() {
        val nodes = List(4) { UIMessage.user("message $it").toMessageNode() }
        val source = Conversation(assistantId = Uuid.random(), messageNodes = nodes)

        val result = source.insertContextCheckpoint(afterNodeId = nodes[1].id, summary = "summary")!!

        assertEquals(nodes.subList(0, 2), result.messageNodes.subList(0, 2))
        assertEquals(nodes.subList(2, 4), result.messageNodes.subList(3, 5))
        val checkpoint = result.messageNodes[2].currentMessage
        assertTrue(checkpoint.isContextCheckpoint)
        assertEquals("summary", checkpoint.toText())
        // 只有检查点之后的消息会继续发送给模型
        assertEquals(result.currentMessages.subList(2, 5), result.currentMessages.limitContext(0))
    }

    @Test
    fun `context checkpoint is not inserted when the anchor node is gone`() {
        val source = Conversation(
            assistantId = Uuid.random(),
            messageNodes = listOf(UIMessage.user("message").toMessageNode()),
        )

        assertNull(source.insertContextCheckpoint(afterNodeId = Uuid.random(), summary = "summary"))
    }

    @Test
    fun `token budget keeps the range when every anchor fits`() {
        val nodes = List(4) { UIMessage.user("message $it").toMessageNode() }

        assertEquals(4, clampEndIndexByTokenBudget(nodes, checkpointIndex = -1, endIndex = 4, tokenBudget = 1000))
    }

    @Test
    fun `token budget drops messages after the first anchor that exceeds it`() {
        val anchor = assistantMessage(contextTokens = 900)
        val overflow = assistantMessage(contextTokens = 2000)
        val nodes = listOf(
            UIMessage.user("message 0").toMessageNode(),
            UIMessage.user("message 1").toMessageNode(),
            anchor,
            overflow,
            UIMessage.user("message 4").toMessageNode(),
        )

        // 900 的锚点放得下，2000 的放不下：endIndex 收缩到 2000 那条之前
        assertEquals(3, clampEndIndexByTokenBudget(nodes, checkpointIndex = -1, endIndex = 5, tokenBudget = 1000))
    }

    @Test
    fun `token budget keeps the range when the first anchor already exceeds it`() {
        val overflow = assistantMessage(contextTokens = 2000)
        val nodes = listOf(overflow, UIMessage.user("message 1").toMessageNode())

        // 第一条可压缩消息的占用就超预算：收缩不出合法范围，保持原范围，让请求发出后暴露真实错误
        assertEquals(2, clampEndIndexByTokenBudget(nodes, checkpointIndex = -1, endIndex = 2, tokenBudget = 1000))
    }

    @Test
    fun `token budget is ignored when the model context is unknown`() {
        val overflow = assistantMessage(contextTokens = 2000)
        val nodes = listOf(UIMessage.user("message 0").toMessageNode(), overflow)

        assertEquals(2, clampEndIndexByTokenBudget(nodes, checkpointIndex = -1, endIndex = 2, tokenBudget = 0))
    }

    private fun assistantMessage(contextTokens: Int) = UIMessage(
        role = MessageRole.ASSISTANT,
        parts = emptyList(),
        lastUsage = TokenUsage(
            promptTokens = contextTokens - 1,
            completionTokens = 1,
        ),
    ).toMessageNode()
}
