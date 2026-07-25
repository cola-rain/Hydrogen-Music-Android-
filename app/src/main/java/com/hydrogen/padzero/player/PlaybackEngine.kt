package com.hydrogen.padzero.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hydrogen.padzero.data.LocalTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class PlaybackEngine(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player: ExoPlayer = ExoPlayer.Builder(appContext).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true,
        )
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                publishSnapshot()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                publishSnapshot()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                publishSnapshot()
            }
        })
    }

    private var queue: List<LocalTrack> = emptyList()
    private var currentIndex: Int = -1
    private val _snapshot = MutableStateFlow(PlaybackSnapshot())
    val snapshot: StateFlow<PlaybackSnapshot> = _snapshot.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                publishSnapshot()
                delay(250)
            }
        }
    }

    fun setQueue(tracks: List<LocalTrack>, startIndex: Int = 0, playImmediately: Boolean = true) {
        queue = tracks.toList()
        currentIndex = startIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0))
        val mediaItems = queue.map { MediaItem.fromUri(it.uri) }
        player.setMediaItems(mediaItems, currentIndex, 0L)
        player.prepare()
        player.playWhenReady = playImmediately
        if (playImmediately) player.play()
        publishSnapshot()
    }

    fun playIndex(index: Int, tracks: List<LocalTrack>? = null) {
        val currentQueue = tracks?.toList() ?: queue
        if (currentQueue.isEmpty()) return
        val safeIndex = index.coerceIn(0, currentQueue.lastIndex)
        if (tracks != null) {
            setQueue(currentQueue, safeIndex, true)
            return
        }
        queue = currentQueue
        currentIndex = safeIndex
        if (player.mediaItemCount != queue.size) {
            setQueue(queue, safeIndex, true)
            return
        }
        player.seekTo(safeIndex, 0L)
        player.playWhenReady = true
        player.play()
        publishSnapshot()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        publishSnapshot()
    }

    fun next() {
        if (queue.isEmpty()) return
        val nextIndex = if (currentIndex + 1 > queue.lastIndex) 0 else currentIndex + 1
        player.seekTo(nextIndex, 0L)
        player.playWhenReady = true
        player.play()
        currentIndex = nextIndex
        publishSnapshot()
    }

    fun previous() {
        if (queue.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) queue.lastIndex else currentIndex - 1
        player.seekTo(prevIndex, 0L)
        player.playWhenReady = true
        player.play()
        currentIndex = prevIndex
        publishSnapshot()
    }

    fun seekToFraction(fraction: Float) {
        val duration = player.duration
        if (duration <= 0L) return
        val safeFraction = fraction.coerceIn(0f, 1f)
        player.seekTo((duration * safeFraction).toLong())
        publishSnapshot()
    }

    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
        publishSnapshot()
    }

    fun loadAndPlay(tracks: List<LocalTrack>, index: Int) {
        setQueue(tracks, index, true)
    }

    fun release() {
        try {
            scope.cancel()
        } catch (_: Throwable) {
        }
        try {
            player.release()
        } catch (_: Throwable) {
        }
    }

    private fun publishSnapshot() {
        val activeIndex = if (queue.isNotEmpty()) {
            player.currentMediaItemIndex.takeIf { it in queue.indices } ?: currentIndex
        } else -1
        if (activeIndex in queue.indices) currentIndex = activeIndex
        val currentTrack = queue.getOrNull(currentIndex)
        val duration = player.duration.takeIf { it > 0L } ?: currentTrack?.durationMs ?: 0L
        _snapshot.value = PlaybackSnapshot(
            currentTrack = currentTrack,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(0L),
            currentIndex = currentIndex,
            queueSize = queue.size,
            volume = player.volume,
        )
    }
}

data class PlaybackSnapshot(
    val currentTrack: LocalTrack? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentIndex: Int = -1,
    val queueSize: Int = 0,
    val volume: Float = 0.3f,
)
