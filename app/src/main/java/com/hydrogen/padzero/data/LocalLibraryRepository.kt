package com.hydrogen.padzero.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.Locale

class LocalLibraryRepository(private val context: Context) {
    suspend fun scanFolder(treeUri: Uri, recursive: Boolean = true): List<LocalTrack> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val tracks = mutableListOf<LocalTrack>()
        walk(root, tracks, recursive)
        tracks.sortBy { it.title.lowercase(Locale.getDefault()) }
        tracks
    }

    private fun walk(directory: DocumentFile, tracks: MutableList<LocalTrack>, recursive: Boolean) {
        if (!directory.isDirectory) return

        val children = runCatching { directory.listFiles().toList() }.getOrDefault(emptyList())
        val sidecarLyricsByBaseName = children
            .asSequence()
            .filter { file -> file.isFile && file.name?.lowercase(Locale.getDefault())?.endsWith(".lrc") == true }
            .associateBy { file -> nameWithoutExtension(file.name ?: "") }

        for (child in children) {
            when {
                child.isDirectory && recursive -> walk(child, tracks, recursive)
                child.isFile && isSupportedAudio(child) -> {
                    val uri = child.uri
                    val metadata = readMetadata(uri)
                    val name = child.name ?: "unknown_audio"
                    val fallbackTitle = nameWithoutExtension(name)
                    val title = metadata?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
                    val artist = metadata?.artist?.takeIf { it.isNotBlank() } ?: ""
                    val album = metadata?.album?.takeIf { it.isNotBlank() } ?: ""
                    val duration = metadata?.durationMs ?: 0L
                    val lyricText = sidecarLyricsByBaseName[nameWithoutExtension(name)]?.let { readTextDocument(it.uri) }

                    tracks += LocalTrack(
                        uri = uri,
                        displayName = name,
                        title = title,
                        artist = artist,
                        album = album,
                        mimeType = child.type,
                        durationMs = duration,
                        lyricText = lyricText,
                    )
                }
            }
        }
    }

    private fun isSupportedAudio(file: DocumentFile): Boolean {
        val name = file.name?.lowercase(Locale.getDefault()) ?: ""
        val mime = file.type?.lowercase(Locale.getDefault()) ?: ""
        val supportedExtensions = listOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "alac", "webm", "mp4", "3gp")
        if (supportedExtensions.any { name.endsWith(".$it") }) return true
        return mime.startsWith("audio/") || mime.contains("mpeg") || mime.contains("mp4") || mime.contains("flac")
    }

    private fun readMetadata(uri: Uri): TrackMetadata? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty()
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).orEmpty()
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            TrackMetadata(title = title, artist = artist, album = album, durationMs = duration)
        } catch (_: Throwable) {
            null
        } finally {
            try { retriever.release() } catch (_: Throwable) { }
        }
    }

    private fun readTextDocument(uri: Uri): String? {
        val bytes = try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArrayOutputStream()
                val data = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(data)
                    if (read <= 0) break
                    buffer.write(data, 0, read)
                }
                buffer.toByteArray()
            }
        } catch (_: Throwable) {
            null
        } ?: return null

        return decodeTextBytes(bytes)
    }

    private fun decodeTextBytes(bytes: ByteArray): String? {
        if (bytes.isEmpty()) return null

        return try {
            when {
                bytes.size >= 3 &&
                    bytes[0] == 0xEF.toByte() &&
                    bytes[1] == 0xBB.toByte() &&
                    bytes[2] == 0xBF.toByte() -> String(bytes, 3, bytes.size - 3, Charsets.UTF_8)

                bytes.size >= 2 &&
                    bytes[0] == 0xFF.toByte() &&
                    bytes[1] == 0xFE.toByte() -> String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)

                bytes.size >= 2 &&
                    bytes[0] == 0xFE.toByte() &&
                    bytes[1] == 0xFF.toByte() -> String(bytes.copyOfRange(2, bytes.size), Charsets.UTF_16BE)

                looksLikeUtf16Le(bytes) -> String(bytes, Charsets.UTF_16LE)
                looksLikeUtf16Be(bytes) -> String(bytes, Charsets.UTF_16BE)
                looksLikeUtf8(bytes) -> String(bytes, Charsets.UTF_8)
                else -> String(bytes, Charset.forName("GB18030"))
            }.normalizeDocumentText()
        } catch (_: Throwable) {
            runCatching { String(bytes, Charsets.UTF_8).normalizeDocumentText() }.getOrNull()
        }
    }

    private fun looksLikeUtf8(bytes: ByteArray): Boolean {
        return try {
            val text = String(bytes, Charsets.UTF_8)
            text.toByteArray(Charsets.UTF_8).contentEquals(bytes)
        } catch (_: Throwable) {
            false
        }
    }

    private fun looksLikeUtf16Le(bytes: ByteArray): Boolean {
        val evenLimit = minOf(bytes.size - (bytes.size % 2), 256)
        if (evenLimit < 4) return false
        var oddNulls = 0
        var evenNulls = 0
        for (index in 0 until evenLimit step 2) {
            if (bytes[index] == 0x00.toByte()) evenNulls++
            if (bytes[index + 1] == 0x00.toByte()) oddNulls++
        }
        val pairs = evenLimit / 2
        val evenRatio = evenNulls.toFloat() / pairs.toFloat()
        val oddRatio = oddNulls.toFloat() / pairs.toFloat()
        return oddRatio >= 0.35f && evenRatio <= 0.1f
    }

    private fun looksLikeUtf16Be(bytes: ByteArray): Boolean {
        val evenLimit = minOf(bytes.size - (bytes.size % 2), 256)
        if (evenLimit < 4) return false
        var oddNulls = 0
        var evenNulls = 0
        for (index in 0 until evenLimit step 2) {
            if (bytes[index] == 0x00.toByte()) evenNulls++
            if (bytes[index + 1] == 0x00.toByte()) oddNulls++
        }
        val pairs = evenLimit / 2
        val evenRatio = evenNulls.toFloat() / pairs.toFloat()
        val oddRatio = oddNulls.toFloat() / pairs.toFloat()
        return evenRatio >= 0.35f && oddRatio <= 0.1f
    }

    private fun String.normalizeDocumentText(): String {
        return replace("\r\n", "\n").replace('\r', '\n')
            .replace("\u0000", "")
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }

    private fun nameWithoutExtension(name: String): String {
        val index = name.lastIndexOf('.')
        return if (index > 0) name.substring(0, index) else name
    }

    private data class TrackMetadata(
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
    )
}
