package me.rerere.rikkahub.utils

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import me.rerere.rikkahub.AppScope
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCheckerTest {

    // 单测跑的是 debug 变体，BuildConfig.DEBUG 为 true
    @Test
    fun `parses latest release and keeps apk assets of the installed build type`() = runBlocking {
        val info = (checkUpdate(
            "/releases/tags/Latest" to """
                {
                  "tag_name": "Latest",
                  "name": "Latest",
                  "prerelease": true,
                  "published_at": "2026-11-22T14:05:09Z",
                  "body": "构建时间：2026-11-22 22:05:03",
                  "assets": [
                    {"name": "app-github-arm64-v8a-debug.apk", "size": 58339216,
                     "browser_download_url": "https://example.com/arm64-debug.apk"},
                    {"name": "app-github-arm64-v8a-release.apk", "size": 15464988,
                     "browser_download_url": "https://example.com/arm64-release.apk"},
                    {"name": "app-github-universal-debug.apk", "size": 61423892,
                     "browser_download_url": "https://example.com/universal-debug.apk"},
                    {"name": "mapping.txt", "size": 188251345,
                     "browser_download_url": "https://example.com/mapping.txt"}
                  ]
                }
            """.trimIndent(),
            "/git/ref/tags/Latest" to """
                {"ref": "refs/tags/Latest", "object": {"sha": "d6b6bb6b3c9d4e5f6a7b8c9d0e1f2a3b4c5d6e7f", "type": "commit"}}
            """.trimIndent(),
        ) as UiState.Success).data

        assertEquals("Latest (d6b6bb6)", info.version)
        assertEquals("d6b6bb6", info.commit)
        assertEquals("2026-11-22T14:05:09Z", info.publishedAt)
        assertEquals("构建时间：2026-11-22 22:05:03", info.changelog)
        assertEquals(
            listOf(
                UpdateDownload("app-github-arm64-v8a-debug.apk", "https://example.com/arm64-debug.apk", "55.6 MB"),
                UpdateDownload("app-github-universal-debug.apk", "https://example.com/universal-debug.apk", "58.6 MB"),
            ),
            info.downloads,
        )
    }

    @Test
    fun `unavailable release surfaces as error`() = runBlocking {
        val state = checkUpdate(code = 404) as UiState.Error

        assertEquals("Failed to fetch update info", state.error.message)
    }

    private suspend fun checkUpdate(vararg responses: Pair<String, String>, code: Int = 200): UiState<UpdateInfo> {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val path = chain.request().url.encodedPath
                val body = responses.firstOrNull { path.endsWith(it.first) }?.second.orEmpty()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("")
                    .body(body.toResponseBody())
                    .build()
            }
            .build()
        val appScope = AppScope()
        try {
            return withTimeout(10_000) {
                UpdateChecker(client, appScope).updateState.first { it !is UiState.Loading }
            }
        } finally {
            appScope.cancel()
        }
    }
}
