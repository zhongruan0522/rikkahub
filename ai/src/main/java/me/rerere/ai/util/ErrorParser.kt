package me.rerere.ai.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class HttpException(
    message: String
) : RuntimeException(message)

fun JsonElement.parseErrorDetail(): HttpException {
    return when (this) {
        is JsonObject -> {
            // 尝试获取常见的错误字段
            val errorFields = listOf("error", "detail", "message", "description")

            // OpenRouter guardrail: error.metadata.patterns 携带命中的注入检测模式,
            // 例如 "ignore all previous instructions"。这里提取出来附到错误信息里,
            // 以便错误卡片提示用户检查注入条目措辞。
            val guardrailPatterns = this["metadata"]
                ?.let { it as? JsonObject }
                ?.get("patterns")
                ?.let { it as? JsonArray }
                ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                ?.filter { it.isNotBlank() }
                ?.takeIf { it.isNotEmpty() }

            // 查找第一个存在的错误字段
            val foundField = errorFields.firstOrNull { this[it] != null }

            if (foundField != null) {
                // 递归解析找到的字段值
                val inner = this[foundField]!!.parseErrorDetail()
                if (guardrailPatterns != null) {
                    HttpException(
                        buildString {
                            append(inner.message)
                            appendLine()
                            append("命中注入检测模式: ")
                            append(guardrailPatterns.joinToString(", "))
                        }
                    )
                } else {
                    inner
                }
            } else {
                // 如果没有找到任何错误字段，序列化整个对象
                HttpException(Json.encodeToString(JsonElement.serializer(), this))
            }
        }

        is JsonArray -> {
            if (this.isEmpty()) {
                HttpException("Unknown error: Empty JSON array")
            } else {
                // 递归解析数组的第一个元素
                this.first().parseErrorDetail()
            }
        }

        is JsonPrimitive -> {
            // 对于基本类型，直接使用其内容
            HttpException(this.jsonPrimitive.content)
        }

        else -> {
            // 其他情况，序列化整个元素
            HttpException(Json.encodeToString(JsonElement.serializer(), this))
        }
    }
}
