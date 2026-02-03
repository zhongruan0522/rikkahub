package me.rerere.ai.provider

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Model(
    val modelId: String = "",
    val displayName: String = "",
    val id: Uuid = Uuid.random(),
    val type: ModelType = ModelType.CHAT,
    val customHeaders: List<CustomHeader> = emptyList(),
    val customBodies: List<CustomBody> = emptyList(),
    val inputModalities: List<Modality> = listOf(Modality.TEXT),
    val outputModalities: List<Modality> = listOf(Modality.TEXT),
    val abilities: List<ModelAbility> = emptyList(),
    val tools: Set<BuiltInTools> = emptySet(),
    // 覆盖内置搜索提供方；为空时根据 modelId 自动识别
    val builtInSearchProvider: BuiltInSearchProvider? = null,
    val providerOverwrite: ProviderSetting? = null,
)

@Serializable
enum class ModelType {
    CHAT,
    IMAGE,
    EMBEDDING,
}

@Serializable
enum class Modality {
    TEXT,
    IMAGE,
}

@Serializable
enum class ModelAbility {
    TOOL,
    REASONING,
}

// 模型(提供商)提供的内置工具选项
@Serializable
sealed class BuiltInTools {
    // https://ai.google.dev/gemini-api/docs/google-search?hl=zh-cn
    @Serializable
    @SerialName("search")
    data object Search : BuiltInTools()

    // https://ai.google.dev/gemini-api/docs/url-context?hl=zh-cn
    @Serializable
    @SerialName("url_context")
    data object UrlContext : BuiltInTools()
}

@Serializable
enum class BuiltInSearchProvider {
    @SerialName("openai")
    OpenAI,

    @SerialName("gemini")
    Gemini,

    @SerialName("claude")
    Claude,
}

fun detectBuiltInSearchProviderFromModelId(modelId: String): BuiltInSearchProvider? {
    val id = modelId.lowercase()
    return when {
        "gemini" in id -> BuiltInSearchProvider.Gemini
        "gpt" in id -> BuiltInSearchProvider.OpenAI
        "claude" in id -> BuiltInSearchProvider.Claude
        else -> null
    }
}

fun Model.resolveBuiltInSearchProvider(defaultProvider: BuiltInSearchProvider? = null): BuiltInSearchProvider? {
    return builtInSearchProvider ?: detectBuiltInSearchProviderFromModelId(modelId) ?: defaultProvider
}



