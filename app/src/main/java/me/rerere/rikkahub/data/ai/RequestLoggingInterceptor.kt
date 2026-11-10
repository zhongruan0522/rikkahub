package me.rerere.rikkahub.data.ai

import me.rerere.common.android.LogEntry
import me.rerere.common.android.Logging
import me.rerere.rikkahub.data.datastore.SettingsStore
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * 请求日志拦截器。
 *
 * 记录请求开关关闭时不做任何事；开启后默认只记录失败的请求（网络异常或非 2xx 响应），
 * 详细模式下记录所有请求的完整元数据（标头、正文、耗时）。
 */
class RequestLoggingInterceptor(
    private val settingsStore: SettingsStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = settingsStore.settingsFlow.value
        if (!settings.requestLoggingEnabled) {
            return chain.proceed(chain.request())
        }
        val detailed = settings.requestLoggingDetailed

        val request = chain.request()
        val startTime = System.currentTimeMillis()

        val requestHeaders = if (detailed) request.headers.toMap() else emptyMap()
        val requestBody = if (detailed) {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
        } else {
            null
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Logging.logRequest(
                LogEntry.RequestLog(
                    tag = "HTTP",
                    url = request.url.toString(),
                    method = request.method,
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    error = e.message ?: e.toString()
                )
            )
            throw e
        }

        if (!detailed && response.isSuccessful) {
            return response
        }

        Logging.logRequest(
            LogEntry.RequestLog(
                tag = "HTTP",
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = response.code,
                responseHeaders = if (detailed) response.headers.toMap() else emptyMap(),
                durationMs = System.currentTimeMillis() - startTime,
            )
        )

        return response
    }

    private fun okhttp3.Headers.toMap(): Map<String, String> {
        return names().associateWith { name ->
            if (name.equals("Proxy-Authorization", ignoreCase = true)) {
                "██"
            } else {
                get(name) ?: ""
            }
        }
    }
}
