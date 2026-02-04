package ruan.rikkahub.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val LegacyPolymorphicTypeNameMap = mapOf(
    // Avatar (package renamed + stabilized with @SerialName)
    "me.rerere.rikkahub.data.model.Avatar.Dummy" to "dummy",
    "me.rerere.rikkohub.data.model.Avatar.Dummy" to "dummy",
    "ruan.rikkahub.data.model.Avatar.Dummy" to "dummy",
    "me.rerere.rikkahub.data.model.Avatar.Emoji" to "emoji",
    "me.rerere.rikkohub.data.model.Avatar.Emoji" to "emoji",
    "ruan.rikkahub.data.model.Avatar.Emoji" to "emoji",
    "me.rerere.rikkahub.data.model.Avatar.Image" to "image",
    "me.rerere.rikkohub.data.model.Avatar.Image" to "image",
    "ruan.rikkahub.data.model.Avatar.Image" to "image",

    // MCP (historical package move + stabilized with @SerialName)
    "me.rerere.rikkahub.data.mcp.McpServerConfig.SseTransportServer" to "sse",
    "ruan.rikkahub.data.mcp.McpServerConfig.SseTransportServer" to "sse",
    "ruan.rikkahub.data.ai.mcp.McpServerConfig.SseTransportServer" to "sse",
    "me.rerere.rikkahub.data.mcp.McpServerConfig.StreamableHTTPServer" to "streamable_http",
    "ruan.rikkahub.data.mcp.McpServerConfig.StreamableHTTPServer" to "streamable_http",
    "ruan.rikkahub.data.ai.mcp.McpServerConfig.StreamableHTTPServer" to "streamable_http",
)

fun JsonElement.migrateLegacyPolymorphicTypes(): JsonElement {
    return when (this) {
        is JsonObject -> JsonObject(entries.associate { (key, value) ->
            if (key == "type" && value is JsonPrimitive && value.isString) {
                val mapped = LegacyPolymorphicTypeNameMap[value.content] ?: value.content
                key to JsonPrimitive(mapped)
            } else {
                key to value.migrateLegacyPolymorphicTypes()
            }
        })

        is JsonArray -> JsonArray(map { it.migrateLegacyPolymorphicTypes() })

        else -> this
    }
}
