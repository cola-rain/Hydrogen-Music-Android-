
package com.hydrogen.padzero.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LocalLibraryRepository(private val context: Context) {
    suspend fun scanFolder(treeUri: Uri): List<LocalTrack> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val tracks = mutableListOf<LocalTrack>()
        walk(root, tracks)
        tracks.sortBy { it.title.lowercase(Locale.getDefault()) }
        tracks
    }

    private fun walk(file: DocumentFile, tracks: MutableList<LocalTrack>) {
        if (file.isDirectory) {
            file.listFiles().forEach { walk(it, tracks) }
            return
        }
        if (!file.isFile) return
        if (!isSupportedAudio(file)) return

        val uri = file.uri
        val metadata = readMetadata(uri)
        val name = file.name ?: "unknown_audio"
        val fallbackTitle = nameWithoutExtension(name)
        val title = metadata?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
        val artist = metadata?.artist?.takeIf { it.isNotBlank() } ?: ""
        val album = metadata?.album?.takeIf { it.isNotBlank() } ?: ""
        val duration = metadata?.durationMs ?: 0L

        tracks += LocalTrack(
            uri = uri,
            displayName = name,
            title = title,
            artist = artist,
            album = album,
            mimeType = file.type,
            durationMs = duration,
        )
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
