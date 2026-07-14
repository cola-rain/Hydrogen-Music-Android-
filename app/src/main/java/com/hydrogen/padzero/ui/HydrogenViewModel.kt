
package com.hydrogen.padzero.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hydrogen.padzero.AppGraph
import com.hydrogen.padzero.data.AppSettings
import com.hydrogen.padzero.data.LocalTrack
import com.hydrogen.padzero.data.ThemeMode
import com.hydrogen.padzero.player.PlaybackSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HydrogenViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = AppGraph.settingsRepository
    private val libraryRepository = AppGraph.libraryRepository
    private val playbackEngine = AppGraph.playbackEngine

    private val _uiState = MutableStateFlow(HydrogenUiState())
    val uiState: StateFlow<HydrogenUiState> = _uiState.asStateFlow()

    val events = MutableSharedFlow<String>(extraBufferCapacity = 16)

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.state.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(settings = settings, folderLabel = settings.lastFolderLabel)
                if (settings.lastFolderUri != null && _uiState.value.tracks.isEmpty() && scanJob == null) {
                    scanSavedFolder(settings.lastFolderUri, settings.lastFolderLabel)
                }
            }
        }
        viewModelScope.launch {
            playbackEngine.snapshot.collectLatest { snapshot ->
                _uiState.value = _uiState.value.copy(playback = snapshot)
            }
        }
        val current = settingsRepository.state.value
        if (current.lastFolderUri != null) {
            scanSavedFolder(current.lastFolderUri, current.lastFolderLabel)
        }
    }

    fun onFolderPicked(uri: Uri, label: String?) {
        val folderLabel = label?.takeIf { it.isNotBlank() } ?: "本地音乐文件夹"
        settingsRepository.setLastFolder(uri.toString(), folderLabel)
        scanFolder(uri, folderLabel)
    }

    fun refreshLibrary() {
        val lastFolder = uiState.value.settings.lastFolderUri ?: return
        scanSavedFolder(lastFolder, uiState.value.settings.lastFolderLabel)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onTrackClicked(track: LocalTrack) {
        val filtered = filteredTracks()
        val index = filtered.indexOfFirst { it.stableId == track.stableId }
        if (index >= 0) {
            playbackEngine.loadAndPlay(filtered, index)
        }
    }

    fun togglePlayPause() = playbackEngine.togglePlayPause()
    fun next() = playbackEngine.next()
    fun previous() = playbackEngine.previous()
    fun seekToFraction(fraction: Float) = playbackEngine.seekToFraction(fraction)
    fun setVolume(volume: Float) = playbackEngine.setVolume(volume)

    fun setOnlineFeaturesEnabled(enabled: Boolean) = settingsRepository.setOnlineFeaturesEnabled(enabled)
    fun setSyncEnabled(enabled: Boolean) = settingsRepository.setSyncEnabled(enabled)
    fun setPreferLocalFirst(enabled: Boolean) = settingsRepository.setPreferLocalFirst(enabled)
    fun setScanSubfolders(enabled: Boolean) = settingsRepository.setScanSubfolders(enabled)
    fun setAllowAtmosFallback(enabled: Boolean) = settingsRepository.setAllowAtmosFallback(enabled)
    fun setThemeMode(mode: ThemeMode) = settingsRepository.setThemeMode(mode)

    fun login() {
        if (!uiState.value.settings.onlineFeaturesEnabled) {
            emitMessage("先在设置里打开“联网功能总开关”。")
            return
        }
        emitMessage("第零版保留登录入口，真正的网易云登录桥接后续再接入。")
    }

    fun syncOnlineState() {
        if (!uiState.value.settings.onlineFeaturesEnabled) {
            emitMessage("联网功能当前关闭。")
            return
        }
        emitMessage("第零版保留同步入口，后续再接入网易云 API。")
    }

    fun searchOnline(keyword: String) {
        if (!uiState.value.settings.onlineFeaturesEnabled) {
            emitMessage("联网搜索当前关闭，只搜索本地库。")
            return
        }
        emitMessage("联网搜索入口已保留：$keyword")
    }

    private fun scanSavedFolder(uriString: String, label: String) {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return
        scanFolder(uri, label)
    }

    private fun scanFolder(uri: Uri, label: String) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, folderLabel = label)
            val tracks = runCatching { libraryRepository.scanFolder(uri) }
                .getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                folderLabel = label,
                tracks = tracks,
            )
            if (tracks.isEmpty()) {
                emitMessage("没有扫描到可播放的音频文件。")
            } else {
                emitMessage("已扫描到 ${tracks.size} 首本地音频。")
            }
        }
        scanJob?.invokeOnCompletion { scanJob = null }
    }

    private fun filteredTracks(): List<LocalTrack> {
        val state = uiState.value
        val q = state.searchQuery.trim().lowercase()
        if (q.isBlank()) return state.tracks
        return state.tracks.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.displayName.lowercase().contains(q)
        }
    }

    private fun emitMessage(text: String) {
        viewModelScope.launch {
            events.emit(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackEngine.release()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HydrogenViewModel(application) as T
            }
        }
    }
}

data class HydrogenUiState(
    val settings: AppSettings = AppSettings(),
    val folderLabel: String = "尚未选择本地音乐文件夹",
    val isScanning: Boolean = false,
    val tracks: List<LocalTrack> = emptyList(),
    val searchQuery: String = "",
    val playback: PlaybackSnapshot = PlaybackSnapshot(),
)
