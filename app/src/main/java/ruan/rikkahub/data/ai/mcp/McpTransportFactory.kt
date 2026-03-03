package ruan.rikkahub.data.ai.mcp

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.util.StringValues
import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport
import ruan.rikkahub.data.ai.mcp.transport.SseClientTransport
import ruan.rikkahub.data.ai.mcp.transport.StreamableHttpClientTransport

internal fun createTransport(
    config: McpServerConfig,
    client: HttpClient,
): AbstractTransport {
    val requestBuilder = config.createRequestBuilder()
    return when (config) {
        is McpServerConfig.SseTransportServer -> SseClientTransport(
            urlString = config.url,
            client = client,
            requestBuilder = requestBuilder,
        )

        is McpServerConfig.StreamableHTTPServer -> StreamableHttpClientTransport(
            url = config.url,
            client = client,
            requestBuilder = requestBuilder,
        )
    }
}

private fun McpServerConfig.createRequestBuilder(): HttpRequestBuilder.() -> Unit = {
    headers.appendAll(StringValues.build {
        commonOptions.headers.forEach { (key, value) ->
            append(key, value)
        }
    })
}

