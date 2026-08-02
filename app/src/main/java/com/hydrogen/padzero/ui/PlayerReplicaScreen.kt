package com.hydrogen.padzero.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.hydrogen.padzero.R
import com.hydrogen.padzero.data.LocalTrack
import com.hydrogen.padzero.data.LyricLine
import com.hydrogen.padzero.data.activeLyricIndex
import com.hydrogen.padzero.data.formatLrcTime
import com.hydrogen.padzero.data.parseLrc
import com.hydrogen.padzero.player.PlaybackSnapshot
import java.util.Locale
import kotlin.math.min

@Composable
fun PlayerReplicaScreen(
    state: HydrogenUiState,
    onBackHome: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDCE9EE)),
    ) {
        val density = LocalDensity.current
        val screenScale = remember(maxWidth, maxHeight) {
            val widthRatio = maxWidth / 1366.dp
            val heightRatio = maxHeight / 768.dp
            min(widthRatio, heightRatio).coerceIn(0.42f, 1f)
        }
        val horizontalScroll = rememberScrollState()
        val verticalScroll = rememberScrollState()

        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density * screenScale,
                fontScale = density.fontScale * screenScale,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .verticalScroll(verticalScroll),
            ) {
                PlayerReplicaCanvas(
                    state = state,
                    onBackHome = onBackHome,
                    onTogglePlay = onTogglePlay,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekToFraction = onSeekToFraction,
                    onVolumeChanged = onVolumeChanged,
                )
            }
        }
    }
}

@Composable
private fun PlayerReplicaCanvas(
    state: HydrogenUiState,
    onBackHome: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
) {
    val currentTrack = state.playback.currentTrack
    val lyricLines = remember(currentTrack?.stableId, currentTrack?.lyricText) {
        parseLrc(currentTrack?.lyricText)
    }
    val activeLyric = activeLyricIndex(lyricLines, state.playback.positionMs)

    Box(
        modifier = Modifier
            .width(1366.dp)
            .height(768.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFDCE9EE), Color(0xFFD5E3E8), Color(0xFFDDEAF0)),
                ),
            )
            .padding(24.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderBar(onBackHome = onBackHome)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CoverAndControlsPanel(
                    modifier = Modifier.width(290.dp).fillMaxHeight(),
                    track = currentTrack,
                    playback = state.playback,
                    onTogglePlay = onTogglePlay,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekToFraction = onSeekToFraction,
                    onVolumeChanged = onVolumeChanged,
                )
                LyricAndCommentPanel(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    lyricLines = lyricLines,
                    activeLyricIndex = activeLyric,
                    currentTrack = currentTrack,
                )
            }
        }
    }
}

@Composable
private fun HeaderBar(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Hydrogen",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1F22),
            )
            Text(
                text = "MUSIC",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = Color(0xFF3A4751),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "播放界面",
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            color = Color(0xFF3A4751),
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(16.dp))
        OutlinedButton(onClick = onBackHome) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("返回主页")
        }
    }
}

@Composable
private fun CoverAndControlsPanel(
    modifier: Modifier,
    track: LocalTrack?,
    playback: PlaybackSnapshot,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0DEE4).copy(alpha = 0.86f)),
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "PLAYING",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = Color(0xFF1C2328),
                fontWeight = FontWeight.Bold,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(266.dp)
                    .border(1.dp, Color(0xFF20272B))
                    .clip(RoundedCornerShape(0.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFBFD0D6), Color(0xFFDDE8EC)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(1.dp, Color(0xFF1D2227).copy(alpha = 0.25f))
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_hydrogen_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(180.dp),
                )
                CornerMarks()
            }

            Text(
                text = track?.title ?: "Operation Fake Waves",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF12171B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track?.subtitle ?: "本地音频",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4D5A63),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(4.dp))
            Slider(
                value = if (playback.durationMs > 0L) (playback.positionMs.toFloat() / playback.durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                onValueChange = onSeekToFraction,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF101418),
                    activeTrackColor = Color(0xFF101418),
                    inactiveTrackColor = Color(0xFF7E8C94),
                ),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(playback.positionMs.prettyTime(), color = Color(0xFF1E262C), style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Text(playback.durationMs.prettyTime(), color = Color(0xFF1E262C), style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null)
                }
                Button(
                    onClick = onTogglePlay,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                }
                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null)
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "VOLUME",
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                color = Color(0xFF20272B),
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = playback.volume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF101418),
                    activeTrackColor = Color(0xFF101418),
                    inactiveTrackColor = Color(0xFF7E8C94),
                ),
            )
        }
    }
}

@Composable
private fun LyricAndCommentPanel(
    modifier: Modifier,
    lyricLines: List<LyricLine>,
    activeLyricIndex: Int,
    currentTrack: LocalTrack?,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF1F4).copy(alpha = 0.88f)),
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            var mode by rememberSaveable { mutableIntStateOf(0) }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = { mode = 0 },
                    label = { Text("LYRIC") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (mode == 0) Color(0xFF101418) else Color.Transparent,
                        labelColor = if (mode == 0) Color.White else Color(0xFF223038),
                    ),
                )
                AssistChip(
                    onClick = { mode = 1 },
                    label = { Text("COMMENTS") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (mode == 1) Color(0xFF101418) else Color.Transparent,
                        labelColor = if (mode == 1) Color.White else Color(0xFF223038),
                    ),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = currentTrack?.title ?: "Operation Fake Waves",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    color = Color(0xFF1D262B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(14.dp))

            if (mode == 0) {
                LyricsPanel(
                    lyricLines = lyricLines,
                    activeLyricIndex = activeLyricIndex,
                )
            } else {
                CommentsPlaceholder()
            }
        }
    }
}

@Composable
private fun LyricsPanel(
    lyricLines: List<LyricLine>,
    activeLyricIndex: Int,
) {
    if (lyricLines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("暂无 .lrc 歌词文件", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Color(0xFF24323A))
                Spacer(Modifier.height(8.dp))
                Text("把和音频同名的 .lrc 文件放在同一文件夹里即可显示歌词。", color = Color(0xFF4F606B))
            }
        }
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(activeLyricIndex, lyricLines.size) {
        val target = when {
            activeLyricIndex < 0 -> 0
            lyricLines.size <= 1 -> 0
            else -> (activeLyricIndex - 3).coerceAtLeast(0)
        }
        runCatching { listState.animateScrollToItem(target) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(lyricLines) { index, line ->
            val selected = index == activeLyricIndex
            val highlight = if (selected) Color(0xFF101418) else Color.Transparent
            val textColor = if (selected) Color.White else Color(0xFF1F272D)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(highlight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column {
                    Text(
                        text = line.text,
                        color = textColor,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatLrcTime(line.timeMs),
                        color = if (selected) Color.White.copy(alpha = 0.7f) else Color(0xFF6D7A82),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "COMMENTS",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = Color(0xFF223038),
                fontWeight = FontWeight.Bold,
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.52f)),
                shape = RoundedCornerShape(0.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("评论区预留位", fontWeight = FontWeight.SemiBold, color = Color(0xFF1D2429))
                    Text(
                        "这一页先保留原版的切换位置，后面再接回网易云评论接口。",
                        color = Color(0xFF4F606B),
                    )
                }
            }
        }
    }
}

@Composable
private fun CornerMarks() {
    val markColor = Color(0xFF101418)
    val size = 10.dp
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.size(size).background(markColor).align(Alignment.TopStart))
        Box(modifier = Modifier.size(size).background(markColor).align(Alignment.TopEnd))
        Box(modifier = Modifier.size(size).background(markColor).align(Alignment.BottomStart))
        Box(modifier = Modifier.size(size).background(markColor).align(Alignment.BottomEnd))
    }
}

private fun Long.prettyTime(): String {
    if (this <= 0L) return "00:00"
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun Float.toVolumePercent(): String = String.format(Locale.getDefault(), "%d%%", (coerceIn(0f, 1f) * 100).toInt())
