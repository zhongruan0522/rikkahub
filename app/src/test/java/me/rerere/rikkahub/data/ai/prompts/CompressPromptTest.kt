package me.rerere.rikkahub.data.ai.prompts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompressPromptTest {
    @Test
    fun `default summary words matches SillyTavern`() {
        assertEquals(200, DEFAULT_SUMMARY_WORDS)
    }

    @Test
    fun `prompt is the SillyTavern default with the word limit substituted`() {
        val prompt = buildCompressPrompt(additionalPrompt = "", summaryWords = 350)

        assertTrue(
            prompt.startsWith(
                "Ignore previous instructions. Summarize the most important facts and events in the story so far."
            )
        )
        assertTrue(
            prompt.contains("If a summary already exists in your memory, use that as a base and expand with new facts.")
        )
        assertTrue(prompt.contains("Limit the summary to 350 words or less."))
        assertTrue(prompt.contains("Your response should include nothing but the summary."))
        // 不再使用结构化摘要的固定小节
        assertFalse(prompt.contains("## User Goals"))
    }

    @Test
    fun `mid turn prompt tells the model how to resume the work`() {
        val prompt = buildCompressPrompt(additionalPrompt = "", summaryWords = 200, midTurn = true)

        assertTrue(prompt.contains("in the middle of your work"))
        assertTrue(prompt.contains("State exactly which steps are already done"))
    }

    @Test
    fun `additional instructions are appended`() {
        val prompt = buildCompressPrompt(additionalPrompt = "Focus on the file paths.", summaryWords = 200)

        assertTrue(prompt.contains("Additional instructions from user: Focus on the file paths."))
    }

    @Test
    fun `blank additional prompt adds nothing`() {
        val prompt = buildCompressPrompt(additionalPrompt = "  ", summaryWords = 200)

        assertFalse(prompt.contains("Additional instructions"))
    }
}
