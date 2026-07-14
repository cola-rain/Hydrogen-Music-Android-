
package com.hydrogen.padzero.data

import android.net.Uri

data class LocalTrack(
    val uri: Uri,
    val displayName: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val mimeType: String? = null,
    val durationMs: Long = 0L,
) {
    val subtitle: String
        get() = when {
            artist.isNotBlank() -> artist
            album.isNotBlank() -> album
            else -> "本地文件"
        }

    val stableId: String
        get() = uri.toString()
}
