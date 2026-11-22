package me.rerere.rikkahub.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.rerere.common.http.await
import me.rerere.rikkahub.AppScope
import me.rerere.rikkahub.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request

private const val RELEASE_API = "https://api.github.com/repos/zhongruan0522/rikkahub/"

class UpdateChecker(
    private val client: OkHttpClient,
    appScope: AppScope,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val updateState: StateFlow<UiState<UpdateInfo>> = checkUpdate().stateIn(
        scope = appScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.Loading,
    )

    private fun checkUpdate(): Flow<UiState<UpdateInfo>> = flow {
        emit(UiState.Loading)
        emit(
            UiState.Success(
                data = try {
                    val release = json.decodeFromString<GitHubRelease>(fetch("${RELEASE_API}releases/tags/Latest"))
                    // Latest 每次 push master 都会被覆盖，没有版本号可比分，拿 tag 指向的 commit 与本机构建比
                    val commit = json.decodeFromString<GitHubRef>(fetch("${RELEASE_API}git/ref/tags/Latest"))
                        .`object`.sha.take(7)
                    UpdateInfo(
                        version = "${release.name} ($commit)",
                        commit = commit,
                        publishedAt = release.publishedAt,
                        changelog = release.body,
                        // debug 与 release 的 applicationId 不同，互相装不上
                        downloads = release.assets
                            .filter { it.name.endsWith(if (BuildConfig.DEBUG) "-debug.apk" else "-release.apk") }
                            .map {
                                UpdateDownload(
                                    name = it.name,
                                    url = it.downloadUrl,
                                    size = it.size.fileSizeToString()
                                )
                            },
                    )
                } catch (e: Exception) {
                    throw Exception("Failed to fetch update info", e)
                }
            )
        )
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(Dispatchers.IO)

    private suspend fun fetch(url: String): String = client.newCall(
        Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", "RikkaHub ${BuildConfig.VERSION_NAME} #${BuildConfig.VERSION_CODE}")
            .build()
    ).await().use {
        if (!it.isSuccessful) {
            throw Exception("Failed to fetch update info")
        }
        it.body.string()
    }

    fun downloadUpdate(context: Context, download: UpdateDownload) {
        runCatching {
            val request = DownloadManager.Request(download.url.toUri()).apply {
                // 设置下载时通知栏的标题和描述
                setTitle(download.name)
                setDescription("正在下载更新包...")
                // 下载完成后通知栏可见
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // 设置文件保存路径
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, download.name)
                // 允许下载的文件类型
                setMimeType("application/vnd.android.package-archive")
            }
            // 获取系统的DownloadManager
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            // 你可以保存返回的downloadId到本地，以便后续查询下载进度或状态
        }.onFailure {
            Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
            context.openUrl(download.url) // 跳转到下载页面
        }
    }
}

data class UpdateDownload(
    val name: String,
    val url: String,
    val size: String
)

data class UpdateInfo(
    val version: String,
    val commit: String,
    val publishedAt: String,
    val changelog: String,
    val downloads: List<UpdateDownload>
)

@Serializable
private data class GitHubRelease(
    val name: String,
    @SerialName("published_at") val publishedAt: String,
    val body: String,
    val assets: List<GitHubAsset>,
)

@Serializable
private data class GitHubAsset(
    val name: String,
    val size: Long,
    @SerialName("browser_download_url") val downloadUrl: String,
)

@Serializable
private data class GitHubRef(
    val `object`: GitHubObject,
)

@Serializable
private data class GitHubObject(
    val sha: String,
)
