package me.rerere.search

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.ai.core.InputSchema
import me.rerere.search.SearchResult.SearchResultItem
import me.rerere.search.SearchService.Companion.httpClient
import me.rerere.search.SearchService.Companion.json
import me.rerere.search.SearchService.Companion.keyRoulette
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object AnySearchService : SearchService<SearchServiceOptions.AnySearchOptions> {
    override val name: String = "AnySearch"

    private const val SEARCH_URL = "https://api.anysearch.com/v1/search"

    @Composable
    override fun Description() {
        val urlHandler = LocalUriHandler.current
        TextButton(
            onClick = {
                urlHandler.openUri("https://www.anysearch.com/docs/api-endpoints/v1-search")
            }
        ) {
            Text(stringResource(R.string.click_to_get_api_key))
        }
    }

    override fun parameters(options: SearchServiceOptions.AnySearchOptions): InputSchema? =
        InputSchema.Obj(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", "string")
                    put("description", SEARCH_QUERY_DESCRIPTION)
                })
            },
            required = listOf("query")
        )

    override fun scrapingParameters(options: SearchServiceOptions.AnySearchOptions): InputSchema? = null

    override suspend fun search(
        params: JsonObject,
        commonOptions: SearchCommonOptions,
        serviceOptions: SearchServiceOptions.AnySearchOptions
    ): Result<SearchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val query = params["query"]?.jsonPrimitive?.content ?: error("query is required")
            val apiKey = keyRoulette.next(serviceOptions.apiKey, serviceOptions.id.toString())

            val body = buildJsonObject {
                put("query", query)
                put("max_results", commonOptions.resultSize.coerceIn(1, 10))
            }

            val request = Request.Builder()
                .url(SEARCH_URL)
                .post(json.encodeToString(body).toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            val response = httpClient.newCall(request).await()
            if (response.isSuccessful) {
                parseResponse(response.body.string())
            } else {
                // 错误响应体可能包含敏感凭据，仅报告状态码
                error("AnySearch search failed with code ${response.code}: ${response.message}")
            }
        }
    }

    override suspend fun scrape(
        params: JsonObject,
        commonOptions: SearchCommonOptions,
        serviceOptions: SearchServiceOptions.AnySearchOptions
    ): Result<ScrapedResult> {
        return Result.failure(Exception("Scraping is not supported for AnySearch"))
    }

    internal fun parseResponse(responseBody: String): SearchResult {
        val response = json.decodeFromString<AnySearchResponse>(responseBody)
        if (response.code != 0) {
            error("AnySearch search failed: ${response.message ?: "unknown error"}")
        }
        val items = response.data?.results.orEmpty().map {
            SearchResultItem(
                title = it.title,
                url = it.url,
                text = it.content ?: it.snippet ?: "",
            )
        }
        return SearchResult(items = items)
    }

    @Serializable
    data class AnySearchResponse(
        val code: Int = 0,
        val message: String? = null,
        @SerialName("request_id") val requestId: String? = null,
        val data: Data? = null,
    ) {
        @Serializable
        data class Data(
            val results: List<SearchItem> = emptyList(),
            val metadata: Metadata? = null,
        )

        @Serializable
        data class SearchItem(
            val title: String = "",
            val url: String = "",
            val snippet: String? = null,
            val content: String? = null,
        )

        @Serializable
        data class Metadata(
            @SerialName("total_results") val totalResults: Int = 0,
            @SerialName("search_time_ms") val searchTimeMs: Int = 0,
        )
    }
}
