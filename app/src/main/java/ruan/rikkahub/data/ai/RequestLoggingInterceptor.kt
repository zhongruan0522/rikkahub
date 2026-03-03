package ruan.rikkahub.data.ai

import me.rerere.common.android.LogEntry
import me.rerere.common.android.Logging
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class RequestLoggingInterceptor : Interceptor {
    constructor(tag: String = DEFAULT_TAG) {
        this.tag = tag
    }

    private val tag: String

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        val requestHeaders = request.headers.toMap()
        val requestBody = request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }

        val response: Response
        var error: String? = null

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            error = e.toLogString()
            Logging.logRequest(
                LogEntry.RequestLog(
                    tag = tag,
                    url = request.url.toString(),
                    method = request.method,
                    requestHeaders = requestHeaders,
                    requestBody = requestBody,
                    error = error
                )
            )
            throw e
        }

        val durationMs = System.currentTimeMillis() - startTime
        val responseHeaders = response.headers.toMap()

        Logging.logRequest(
            LogEntry.RequestLog(
                tag = tag,
                url = request.url.toString(),
                method = request.method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                responseCode = response.code,
                responseHeaders = responseHeaders,
                durationMs = durationMs,
                error = error
            )
        )

        return response
    }

    private fun okhttp3.Headers.toMap(): Map<String, String> {
        return names().associateWith { get(it) ?: "" }
    }

    private fun Exception.toLogString(): String {
        val exceptionName = this::class.qualifiedName ?: javaClass.name
        val message = message?.takeIf { it.isNotBlank() } ?: return exceptionName
        return "$exceptionName: $message"
    }

    private companion object {
        const val DEFAULT_TAG = "HTTP"
    }
}
