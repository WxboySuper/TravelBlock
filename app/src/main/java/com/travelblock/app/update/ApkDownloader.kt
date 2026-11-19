package com.travelblock.app.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.net.URL

class ApkDownloader(context: Context) {
    private val cacheDirectory = File(context.cacheDir, "updates")
    var file: File? = null
        private set

    fun download(info: UpdateInfo): Flow<DownloadProgress> = flow {
        val destination = newDestination(info.build)
        try {
            transfer(info, destination) { emit(it) }
            file = destination
        } catch (error: Throwable) {
            destination.delete()
            throw error
        }
    }.flowOn(Dispatchers.IO)

    private fun newDestination(build: Int): File {
        cacheDirectory.mkdirs()
        cacheDirectory.listFiles()?.forEach(File::delete)
        return File(cacheDirectory, "TravelBlock-$build.apk")
    }

    private suspend fun transfer(info: UpdateInfo, destination: File, report: suspend (DownloadProgress) -> Unit) {
        val connection = URL(info.apkUrl).openConnection().apply { connectTimeout = 5_000; readTimeout = 5_000 }
        val expected = info.sizeBytes.takeIf { it > 0 } ?: connection.contentLengthLong
        connection.getInputStream().use { input -> destination.outputStream().use { output ->
            report(DownloadProgress(0, expected))
            copyBytes(input, output, expected, report)
        } }
    }

    private suspend fun copyBytes(
        input: java.io.InputStream,
        output: java.io.OutputStream,
        expected: Long,
        report: suspend (DownloadProgress) -> Unit,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var received = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
            received += count
            report(DownloadProgress(received, expected))
        }
        require(expected <= 0 || received == expected) { "APK size mismatch" }
    }
}
