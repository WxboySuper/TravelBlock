package com.travelblock.app.update

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.io.IOException
import java.net.URL

data class UpdateInfo(
    val build: Int,
    val sha: String,
    val apkUrl: String,
    val sizeBytes: Long,
    val pageUrl: String,
    val publishedAt: String,
)

class MissingApkAssetException(val pageUrl: String) : IOException("TravelBlock.apk is missing from the latest release")

object UpdateChecker {
    const val ReleaseApi = "https://api.github.com/repos/WxboySuper/TravelBlock/releases/tags/latest"
    private val json = Json { ignoreUnknownKeys = true }
    const val CheckIntervalMillis = 15 * 60 * 1000L

    fun shouldCheck(lastCheckMillis: Long, nowMillis: Long): Boolean =
        nowMillis - lastCheckMillis >= CheckIntervalMillis

    fun parse(body: String, installedBuild: Int): UpdateInfo? = runCatching {
        val root = json.parseToJsonElement(body).jsonObject
        val firstLine = root["body"]?.jsonPrimitive?.content?.lineSequence()?.firstOrNull().orEmpty()
        val match = Regex("^build=(\\d+) sha=([^ ]+) published=(\\S+)$").matchEntire(firstLine) ?: return null
        val asset = root["assets"]?.jsonArray?.firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.content == "TravelBlock.apk" }?.jsonObject ?: return null
        val build = match.groupValues[1].toIntOrNull() ?: return null
        if (build <= installedBuild) return null
        UpdateInfo(
            build = build,
            sha = match.groupValues[2],
            apkUrl = asset["browser_download_url"]?.jsonPrimitive?.content ?: return null,
            sizeBytes = asset["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
            pageUrl = root["html_url"]?.jsonPrimitive?.content ?: return null,
            publishedAt = match.groupValues[3],
        )
    }.getOrNull()

    suspend fun check(installedBuild: Int): UpdateInfo? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val connection = URL(ReleaseApi).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.setRequestProperty("User-Agent", "TravelBlock-Android-Updater")
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.inputStream.bufferedReader().use { reader ->
                val body = reader.readText()
                val info = parse(body, installedBuild)
                if (info != null) return@use info
                val root = json.parseToJsonElement(body).jsonObject
                val releaseBody = root["body"]?.jsonPrimitive?.content.orEmpty()
                val build = releaseBody.lineSequence().firstOrNull()?.let(::buildFromLine) ?: return@use null
                if (build > installedBuild) {
                    val page = root["html_url"]?.jsonPrimitive?.content ?: return@use null
                    throw MissingApkAssetException(page)
                }
                null
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun buildFromLine(line: String): Int? = Regex("^build=(\\d+) sha=([^ ]+) published=(\\S+)$")
        .matchEntire(line)?.groupValues?.get(1)?.toIntOrNull()
}
