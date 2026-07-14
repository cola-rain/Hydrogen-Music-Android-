
package com.hydrogen.padzero.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hydrogen.padzero.data.LocalTrack
import com.hydrogen.padzero.data.ThemeMode
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HydrogenApp(viewModel: HydrogenViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var moreMenuOpen by rememberSaveable { mutableStateOf(false) }
    var pendingOnlineDialog by rememberSaveable { mutableStateOf(false) }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Throwable) {
            }
            val label = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
            viewModel.onFolderPicked(uri, label)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { text ->
            snackbarHostState.showSnackbar(text)
        }
    }

    val filteredTracks = remember(state.tracks, state.searchQuery) {
        val q = state.searchQuery.trim().lowercase(Locale.getDefault())
        if (q.isBlank()) state.tracks else state.tracks.filter {
            it.title.lowercase(Locale.getDefault()).contains(q) ||
            it.artist.lowercase(Locale.getDefault()).contains(q) ||
            it.displayName.lowercase(Locale.getDefault()).contains(q)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val wide = maxWidth >= 900.dp
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text("Hydrogen Pad Zero", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = state.folderLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { pendingOnlineDialog = true }) {
                        Text(if (state.settings.onlineFeaturesEnabled) "联网已开" else "联网关闭")
                    }
                    IconButton(onClick = { settingsOpen = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                    Box {
                        IconButton(onClick = { moreMenuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(expanded = moreMenuOpen, onDismissRequest = { moreMenuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("选择本地文件夹") },
                                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                onClick = {
                                    moreMenuOpen = false
                                    openFolderLauncher.launch(null)
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("刷新本地库") },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    moreMenuOpen = false
                                    viewModel.refreshLibrary()
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )

            Box(modifier = Modifier.weight(1f)) {
                if (wide) {
                    WideLayout(
                        state = state,
                        tracks = filteredTracks,
                        onSelectFolder = { openFolderLauncher.launch(null) },
                        onTrackClicked = viewModel::onTrackClicked,
                        onSearchChanged = viewModel::onSearchQueryChanged,
                        onTogglePlay = viewModel::togglePlayPause,
                        onNext = viewModel::next,
                        onPrevious = viewModel::previous,
                        onSeekToFraction = viewModel::seekToFraction,
                        onVolumeChanged = viewModel::setVolume,
                        onLogin = viewModel::login,
                        onSync = viewModel::syncOnlineState,
                        onOnlineToggle = viewModel::setOnlineFeaturesEnabled,
                        onSyncToggle = viewModel::setSyncEnabled,
                        onPreferLocalToggle = viewModel::setPreferLocalFirst,
                        onScanSubfoldersToggle = viewModel::setScanSubfolders,
                        onAtmosFallbackToggle = viewModel::setAllowAtmosFallback,
                        onThemeModeChange = viewModel::setThemeMode,
                    )
                } else {
                    CompactLayout(
                        state = state,
                        tracks = filteredTracks,
                        onSelectFolder = { openFolderLauncher.launch(null) },
                        onTrackClicked = viewModel::onTrackClicked,
                        onSearchChanged = viewModel::onSearchQueryChanged,
                        onTogglePlay = viewModel::togglePlayPause,
                        onNext = viewModel::next,
                        onPrevious = viewModel::previous,
                        onSeekToFraction = viewModel::seekToFraction,
                        onVolumeChanged = viewModel::setVolume,
                        onLogin = viewModel::login,
                        onSync = viewModel::syncOnlineState,
                        onOnlineToggle = viewModel::setOnlineFeaturesEnabled,
                        onSyncToggle = viewModel::setSyncEnabled,
                        onPreferLocalToggle = viewModel::setPreferLocalFirst,
                        onScanSubfoldersToggle = viewModel::setScanSubfolders,
                        onAtmosFallbackToggle = viewModel::setAllowAtmosFallback,
                        onThemeModeChange = viewModel::setThemeMode,
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                )
            }
        }
    }

    if (settingsOpen) {
        SettingsSheet(
            settings = state.settings,
            onDismiss = { settingsOpen = false },
            onOnlineToggle = viewModel::setOnlineFeaturesEnabled,
            onSyncToggle = viewModel::setSyncEnabled,
            onPreferLocalToggle = viewModel::setPreferLocalFirst,
            onScanSubfoldersToggle = viewModel::setScanSubfolders,
            onAtmosFallbackToggle = viewModel::setAllowAtmosFallback,
            onThemeModeChange = viewModel::setThemeMode,
            onLogin = viewModel::login,
            onSync = viewModel::syncOnlineState,
        )
    }

    if (pendingOnlineDialog) {
        AlertDialog(
            onDismissRequest = { pendingOnlineDialog = false },
            title = { Text("联网功能说明") },
            text = {
                Text("第零版优先实现本地播放。登录、网易 API、同步都保留开关和入口，但暂时仍是占位。")
            },
            confirmButton = {
                TextButton(onClick = { pendingOnlineDialog = false }) { Text("知道了") }
            },
        )
    }
}

@Composable
private fun WideLayout(
    state: HydrogenUiState,
    tracks: List<LocalTrack>,
    onSelectFolder: () -> Unit,
    onTrackClicked: (LocalTrack) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,
    onOnlineToggle: (Boolean) -> Unit,
    onSyncToggle: (Boolean) -> Unit,
    onPreferLocalToggle: (Boolean) -> Unit,
    onScanSubfoldersToggle: (Boolean) -> Unit,
    onAtmosFallbackToggle: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LeftPanel(
            modifier = Modifier.weight(0.95f),
            state = state,
            onSelectFolder = onSelectFolder,
            onSearchChanged = onSearchChanged,
            onLogin = onLogin,
            onSync = onSync,
            onOnlineToggle = onOnlineToggle,
        )
        TrackListPanel(
            modifier = Modifier.weight(1.6f),
            tracks = tracks,
            selectedIndex = state.playback.currentIndex,
            isScanning = state.isScanning,
            onTrackClicked = onTrackClicked,
        )
        PlayerPanel(
            modifier = Modifier.weight(1.05f),
            state = state,
            onTogglePlay = onTogglePlay,
            onNext = onNext,
            onPrevious = onPrevious,
            onSeekToFraction = onSeekToFraction,
            onVolumeChanged = onVolumeChanged,
            onPreferLocalToggle = onPreferLocalToggle,
            onScanSubfoldersToggle = onScanSubfoldersToggle,
            onAtmosFallbackToggle = onAtmosFallbackToggle,
            onThemeModeChange = onThemeModeChange,
        )
    }
}

@Composable
private fun CompactLayout(
    state: HydrogenUiState,
    tracks: List<LocalTrack>,
    onSelectFolder: () -> Unit,
    onTrackClicked: (LocalTrack) -> Unit,
    onSearchChanged: (String) -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,
    onOnlineToggle: (Boolean) -> Unit,
    onSyncToggle: (Boolean) -> Unit,
    onPreferLocalToggle: (Boolean) -> Unit,
    onScanSubfoldersToggle: (Boolean) -> Unit,
    onAtmosFallbackToggle: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LeftPanel(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onSelectFolder = onSelectFolder,
            onSearchChanged = onSearchChanged,
            onLogin = onLogin,
            onSync = onSync,
            onOnlineToggle = onOnlineToggle,
        )
        PlayerPanel(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onTogglePlay = onTogglePlay,
            onNext = onNext,
            onPrevious = onPrevious,
            onSeekToFraction = onSeekToFraction,
            onVolumeChanged = onVolumeChanged,
            onPreferLocalToggle = onPreferLocalToggle,
            onScanSubfoldersToggle = onScanSubfoldersToggle,
            onAtmosFallbackToggle = onAtmosFallbackToggle,
            onThemeModeChange = onThemeModeChange,
        )
        TrackListPanel(
            modifier = Modifier.fillMaxWidth().weight(1f),
            tracks = tracks,
            selectedIndex = state.playback.currentIndex,
            isScanning = state.isScanning,
            onTrackClicked = onTrackClicked,
        )
    }
}

@Composable
private fun LeftPanel(
    modifier: Modifier,
    state: HydrogenUiState,
    onSelectFolder: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,
    onOnlineToggle: (Boolean) -> Unit,
) {
    Card(modifier = modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("本地库与联网入口", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("第零版先把本地音乐做稳。联网相关功能保留开关和入口，后续再接真实桥接。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onSelectFolder, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("选择本地音乐文件夹")
            }
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索本地歌曲") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            AssistChip(
                onClick = onLogin,
                enabled = state.settings.onlineFeaturesEnabled,
                label = { Text(if (state.settings.onlineFeaturesEnabled) "登录入口" else "联网关闭") },
                leadingIcon = { Icon(Icons.Default.Login, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(),
            )
            AssistChip(
                onClick = onSync,
                enabled = state.settings.onlineFeaturesEnabled,
                label = { Text("同步入口") },
                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Switch(checked = state.settings.onlineFeaturesEnabled, onCheckedChange = onOnlineToggle)
                Column {
                    Text("联网功能总开关", fontWeight = FontWeight.Medium)
                    Text("关闭时只保留本地播放。", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Divider()
            InfoLine("文件夹", state.folderLabel)
            InfoLine("曲目数", state.tracks.size.toString())
            InfoLine("状态", if (state.isScanning) "正在扫描..." else "就绪")
            if (state.playback.currentTrack != null) {
                InfoLine("当前播放", state.playback.currentTrack.title)
            }
        }
    }
}

@Composable
private fun TrackListPanel(
    modifier: Modifier,
    tracks: List<LocalTrack>,
    selectedIndex: Int,
    isScanning: Boolean,
    onTrackClicked: (LocalTrack) -> Unit,
) {
    Card(modifier = modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("歌曲列表", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
                Text("共 ${tracks.size} 首", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            if (tracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("还没有扫描到本地音频", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("点右上角选择文件夹，或者先把音频放进可访问的目录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(tracks, key = { it.stableId }) { track ->
                        val index = tracks.indexOf(track)
                        TrackCard(track = track, selected = index == selectedIndex, onClick = { onTrackClicked(track) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackCard(track: LocalTrack, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(track.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(12.dp))
            Text(track.durationText(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerPanel(
    modifier: Modifier,
    state: HydrogenUiState,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekToFraction: (Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onPreferLocalToggle: (Boolean) -> Unit,
    onScanSubfoldersToggle: (Boolean) -> Unit,
    onAtmosFallbackToggle: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Card(modifier = modifier.fillMaxSize(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("播放器与设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (state.playback.currentTrack != null) {
                Text(state.playback.currentTrack.title, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(state.playback.currentTrack.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("还没有开始播放", style = MaterialTheme.typography.headlineSmall)
                Text("选中一首本地歌曲就能开播。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Slider(
                        value = if (state.playback.durationMs > 0L) (state.playback.positionMs.toFloat() / state.playback.durationMs.toFloat()).coerceIn(0f, 1f) else 0f,
                        onValueChange = onSeekToFraction,
                        colors = SliderDefaults.colors(),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(state.playback.positionMs.prettyTime())
                        Text(state.playback.durationMs.prettyTime())
                    }
                    Slider(
                        value = state.playback.volume,
                        onValueChange = onVolumeChanged,
                        valueRange = 0f..1f,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("音量")
                        Text(state.playback.volume.toVolumePercent())
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onPrevious, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("上一首")
                }
                Button(onClick = onTogglePlay, modifier = Modifier.weight(1f)) {
                    Icon(if (state.playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.playback.isPlaying) "暂停" else "播放")
                }
                OutlinedButton(onClick = onNext, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.SkipNext, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("下一首")
                }
            }

            Divider()
            Text("第零版开关", fontWeight = FontWeight.SemiBold)
            SettingToggle("优先本地播放", state.settings.preferLocalFirst, onPreferLocalToggle)
            SettingToggle("扫描子文件夹", state.settings.scanSubfolders, onScanSubfoldersToggle)
            SettingToggle("Dolby/Atmos 自动回退", state.settings.allowAtmosFallback, onAtmosFallbackToggle)
            ThemeSwitchRow(current = state.settings.themeMode, onThemeModeChange = onThemeModeChange)
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium)
            Text(if (checked) "已开启" else "已关闭", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeSwitchRow(current: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("主题")
        ThemeChip("系统", current == ThemeMode.SYSTEM) { onThemeModeChange(ThemeMode.SYSTEM) }
        ThemeChip("浅色", current == ThemeMode.LIGHT) { onThemeModeChange(ThemeMode.LIGHT) }
        ThemeChip("深色", current == ThemeMode.DARK) { onThemeModeChange(ThemeMode.DARK) }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
        ),
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsSheet(
    settings: com.hydrogen.padzero.data.AppSettings,
    onDismiss: () -> Unit,
    onOnlineToggle: (Boolean) -> Unit,
    onSyncToggle: (Boolean) -> Unit,
    onPreferLocalToggle: (Boolean) -> Unit,
    onScanSubfoldersToggle: (Boolean) -> Unit,
    onAtmosFallbackToggle: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            SettingToggle("联网功能总开关", settings.onlineFeaturesEnabled, onOnlineToggle)
            SettingToggle("联网同步", settings.syncEnabled, onSyncToggle)
            SettingToggle("优先本地播放", settings.preferLocalFirst, onPreferLocalToggle)
            SettingToggle("扫描子文件夹", settings.scanSubfolders, onScanSubfoldersToggle)
            SettingToggle("Dolby/Atmos 自动回退", settings.allowAtmosFallback, onAtmosFallbackToggle)
            ThemeSwitchRow(current = settings.themeMode, onThemeModeChange = onThemeModeChange)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onLogin, enabled = settings.onlineFeaturesEnabled) { Text("登录入口") }
                OutlinedButton(onClick = onSync, enabled = settings.onlineFeaturesEnabled) { Text("同步入口") }
                OutlinedButton(onClick = onDismiss) { Text("关闭") }
            }
            Spacer(Modifier.height(12.dp))
        }
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

private fun LocalTrack.durationText(): String = if (durationMs > 0L) durationMs.prettyTime() else "--:--"
