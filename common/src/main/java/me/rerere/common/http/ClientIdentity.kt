package me.rerere.common.http

import okhttp3.Interceptor
import okhttp3.Response

object ClientIdentity {
    const val HEADER_X_TITLE = "X-Title"
    const val HEADER_HTTP_REFERER = "HTTP-Referer"
    const val HEADER_USER_AGENT = "User-Agent"

    const val X_TITLE = "Cherry Studio"
    const val HTTP_REFERER = "https://cherry-ai.com"

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "CherryStudio/1.7.18 Chrome/140.0.7339.249 Electron/38.7.0 Safari/537.36"
}

class ClientIdentityInterceptor(
    private val xTitle: String = ClientIdentity.X_TITLE,
    private val httpReferer: String = ClientIdentity.HTTP_REFERER,
    private val userAgent: String = ClientIdentity.USER_AGENT,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val updatedRequest = request.newBuilder()
            .header(ClientIdentity.HEADER_X_TITLE, xTitle)
            .header(ClientIdentity.HEADER_HTTP_REFERER, httpReferer)
            .header(ClientIdentity.HEADER_USER_AGENT, userAgent)
            .build()
        return chain.proceed(updatedRequest)
    }
}

