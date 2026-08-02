package com.hydrogen.padzero.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider as RuntimeCompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
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

    val uiScale = state.settings.uiScale.coerceIn(0.75f, 1.0f)
    val currentDensity = LocalDensity.current

    RuntimeCompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density * uiScale,
            fontScale = currentDensity.fontScale * uiScale,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            val wide = maxWidth >= 900.dp
            if (state.screenMode == AppScreenMode.HOME) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Hydrogen Music", fontWeight = FontWeight.SemiBold)
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
                            TextButton(onClick = viewModel::openPlayerScreen) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("播放器")
                            }
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
                                DropdownMenu(
                                    expanded = moreMenuOpen,
                                    onDismissRequest = { moreMenuOpen = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("刷新本地库") },
                                        onClick = {
                                            moreMenuOpen = false
                                            viewModel.refreshLibrary()
                                        },
                                    )
                                }
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
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
                                onUiScaleChange = viewModel::setUiScale,
                                onThemeModeChange = viewModel::setThemeMode,
                                onOpenPlayer = viewModel::openPlayerScreen,
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
                                onUiScaleChange = viewModel::setUiScale,
                                onThemeModeChange = viewModel::setThemeMode,
                                onOpenPlayer = viewModel::openPlayerScreen,
                            )
                        }
                    }
                }
            } else {
                PlayerReplicaScreen(
                    state = state,
                    onBackHome = viewModel::openHomeScreen,
                    onTogglePlay = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onSeekToFraction = viewModel::seekToFraction,
                    onVolumeChanged = viewModel::setVolume,
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
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
            onUiScaleChange = viewModel::setUiScale,
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
    onUiScaleChange: (Float) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeHighlights(modifier = Modifier.fillMaxWidth(), onOpenPlayer = onOpenPlayer)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MyMusicPanel(
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
                onUiScaleChange = onUiScaleChange,
                onThemeModeChange = onThemeModeChange,
            )
        }
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
    onUiScaleChange: (Float) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeHighlights(modifier = Modifier.fillMaxWidth(), onOpenPlayer = onOpenPlayer)
        MyMusicPanel(
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
            onUiScaleChange = onUiScaleChange,
            onThemeModeChange = onThemeModeChange,
        )
        TrackListPanel(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            tracks = tracks,
            selectedIndex = state.playback.currentIndex,
            isScanning = state.isScanning,
            onTrackClicked = onTrackClicked,
        )
    }
}

@Composable
private fun HomeHighlights(
    modifier: Modifier = Modifier,
    onOpenPlayer: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("这是一个封面", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("某神秘歌单名称", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text("先用占位内容保持原版首页的视觉节奏。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onOpenPlayer) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("打开播放器")
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("推荐入口", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("私人漫游 / 塞壬唱片 / 本地音乐 / 搜索结果", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("私人漫游") })
                            AssistChip(onClick = {}, label = { Text("塞壬唱片") })
                            AssistChip(onClick = {}, label = { Text("本地音乐") })
                            AssistChip(onClick = {}, label = { Text("评论区") })
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MiniHomeCard("今日推荐", "占位歌单封面", Modifier.weight(1f))
                MiniHomeCard("每日推荐", "某神秘歌单", Modifier.weight(1f))
                MiniHomeCard("云盘", "先保留按钮", Modifier.weight(1f))
                MiniHomeCard("本地音乐", "在我的音乐里扫描", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniHomeCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MyMusicPanel(
    modifier: Modifier,
    state: HydrogenUiState,
    onSelectFolder: () -> Unit,
    onSearchChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,
    onOnlineToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("我的音乐", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("本地音乐界面放在这里，未登录也能直接进入。", color = MaterialTheme.colorScheme.onSurfaceVariant)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("本地音乐") }, leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) })
                AssistChip(onClick = {}, label = { Text("云盘") }, leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) })
                AssistChip(onClick = {}, label = { Text("下载") }, leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) })
                AssistChip(onClick = {}, label = { Text("联网开关") }, leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) })
            }

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onLogin, enabled = state.settings.onlineFeaturesEnabled, label = { Text("登录入口") }, leadingIcon = { Icon(Icons.Default.Login, contentDescription = null) })
                AssistChip(onClick = onSync, enabled = state.settings.onlineFeaturesEnabled, label = { Text("同步入口") }, leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) })
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
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("歌曲列表", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("共 ${tracks.size} 首", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            if (tracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("还没有扫描到本地音频", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("点“选择本地音乐文件夹”开始扫描。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
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
    onUiScaleChange: (Float) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
    ) {
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
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("界面缩放", fontWeight = FontWeight.Medium)
                    Text(String.format(Locale.getDefault(), "%.0f%%", state.settings.uiScale.coerceIn(0.75f, 1.0f) * 100f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Slider(
                    value = state.settings.uiScale.coerceIn(0.75f, 1.0f),
                    onValueChange = onUiScaleChange,
                    valueRange = 0.75f..1.0f,
                    steps = 4,
                )
                Text("更小的缩放能让低分辨率设备一次显示更多内容。", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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

private fun Long.prettyTime(): String {
    if (this <= 0L) return "00:00"
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun Float.toVolumePercent(): String = String.format(Locale.getDefault(), "%d%%", (coerceIn(0f, 1f) * 100).toInt())

private fun LocalTrack.durationText(): String = if (durationMs > 0L) durationMs.prettyTime() else "--:--"
