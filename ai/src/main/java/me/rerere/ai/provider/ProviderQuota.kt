package me.rerere.ai.provider

import kotlinx.serialization.Serializable

/**
 * 提供商的额度状态，由 [Provider.getQuota] 返回。
 *
 * 按语义而不是按提供商分类型，界面按类型分别展示；新的提供商优先复用已有类型。
 */
sealed interface ProviderQuota {
    /** 余额。用户自定义查询（[QuotaQuery]）的结果都归到这里 */
    data class Balance(val value: String) : ProviderQuota
}

/**
 * 用户自定义的额度查询，只有 API Key 类提供商有。
 *
 * [url]、[headers] 的值和 [body] 可以引用 [VARIABLES] 里的变量，写作 `{{apiKey}}`。
 * [body] 只在 [method] 为 POST 时发送，按 JSON 发出。
 * 查询结果按 [resultPath] 从响应 JSON 里取值，表达式语法见 `evaluateJsonExpr`。
 */
@Serializable
data class QuotaQuery(
    val enabled: Boolean = false,
    val method: QuotaQueryMethod = QuotaQueryMethod.GET,
    val url: String = "{{baseUrl}}/credits",
    // 鉴权头也在这里配置，默认值按提供商类型给
    val headers: List<CustomHeader> = listOf(CustomHeader("Authorization", "Bearer {{apiKey}}")),
    val body: String = "",
    val resultPath: String = "data.total_usage",
) {
    companion object {
        const val VARIABLE_BASE_URL = "baseUrl"

        // 填了多个 key 时取其中一个，和对话请求一样轮换
        const val VARIABLE_API_KEY = "apiKey"

        val VARIABLES = listOf(VARIABLE_BASE_URL, VARIABLE_API_KEY)
    }
}

@Serializable
enum class QuotaQueryMethod {
    GET,
    POST,
}

/** 是否有额度可查，界面据此决定要不要显示 */
val ProviderSetting.supportsQuota: Boolean
    get() = when (this) {
        is ProviderSetting.ApiKey -> quotaQuery.enabled
    }
