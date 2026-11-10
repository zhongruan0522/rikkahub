package me.rerere.rikkahub.data.ai.prompts

/** 摘要词数上限默认值，与 SillyTavern 的 promptWords 默认一致 */
internal const val DEFAULT_SUMMARY_WORDS = 200

/**
 * 压缩指令，作为一条 user 消息追加在对话末尾，模型直接总结上方的对话历史。
 * 摘要会替代之前的全部历史；上一份摘要就在上下文里（压缩请求与对话同构），模型会以它为基础增量扩展。
 *
 * [midTurn] 表示压缩发生在一轮回复的中途：压缩完模型只凭摘要接着干活，摘要得写清楚做到哪了。
 */
internal fun buildCompressPrompt(
    additionalPrompt: String,
    summaryWords: Int,
    midTurn: Boolean = false,
): String = buildString {
    appendLine(
        """
        Ignore previous instructions. Summarize the most important facts and events in the story so far. If a summary already exists in your memory, use that as a base and expand with new facts. Limit the summary to $summaryWords words or less. Your response should include nothing but the summary.
        """.trimIndent()
    )
    if (midTurn) {
        appendLine()
        appendLine(
            """
            This summary is being written in the middle of your work on the user's latest request, because the context is almost full. Right after it you will continue that work with only the summary as context, and the user will not see or answer anything in between. State exactly which steps are already done, which remain, and the very next action to take, so the work resumes without repeating finished steps.
            """.trimIndent()
        )
    }
    if (additionalPrompt.isNotBlank()) {
        appendLine()
        appendLine("Additional instructions from user: $additionalPrompt")
    }
}.trimEnd()
