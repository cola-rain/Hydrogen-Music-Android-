
package com.hydrogen.padzero.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    fun setOnlineFeaturesEnabled(enabled: Boolean) = update { it.copy(onlineFeaturesEnabled = enabled) }
    fun setSyncEnabled(enabled: Boolean) = update { it.copy(syncEnabled = enabled) }
    fun setPreferLocalFirst(enabled: Boolean) = update { it.copy(preferLocalFirst = enabled) }
    fun setScanSubfolders(enabled: Boolean) = update { it.copy(scanSubfolders = enabled) }
    fun setAllowAtmosFallback(enabled: Boolean) = update { it.copy(allowAtmosFallback = enabled) }
    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setLastFolder(uri: String?, label: String) = update { it.copy(lastFolderUri = uri, lastFolderLabel = label) }
    fun clearFolder() = update { it.copy(lastFolderUri = null, lastFolderLabel = "尚未选择本地音乐文件夹") }

    private fun update(block: (AppSettings) -> AppSettings) {
        val next = block(_state.value)
        persist(next)
        _state.value = next
    }

    private fun load(): AppSettings {
        return AppSettings(
            onlineFeaturesEnabled = prefs.getBoolean(KEY_ONLINE, false),
            syncEnabled = prefs.getBoolean(KEY_SYNC, false),
            preferLocalFirst = prefs.getBoolean(KEY_PREFER_LOCAL_FIRST, true),
            scanSubfolders = prefs.getBoolean(KEY_SCAN_SUBFOLDERS, true),
            allowAtmosFallback = prefs.getBoolean(KEY_ALLOW_ATMOS_FALLBACK, true),
            themeMode = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            lastFolderUri = prefs.getString(KEY_LAST_FOLDER_URI, null),
            lastFolderLabel = prefs.getString(KEY_LAST_FOLDER_LABEL, "尚未选择本地音乐文件夹") ?: "尚未选择本地音乐文件夹",
        )
    }

    private fun persist(settings: AppSettings) {
        prefs.edit()
            .putBoolean(KEY_ONLINE, settings.onlineFeaturesEnabled)
            .putBoolean(KEY_SYNC, settings.syncEnabled)
            .putBoolean(KEY_PREFER_LOCAL_FIRST, settings.preferLocalFirst)
            .putBoolean(KEY_SCAN_SUBFOLDERS, settings.scanSubfolders)
            .putBoolean(KEY_ALLOW_ATMOS_FALLBACK, settings.allowAtmosFallback)
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putString(KEY_LAST_FOLDER_URI, settings.lastFolderUri)
            .putString(KEY_LAST_FOLDER_LABEL, settings.lastFolderLabel)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "hydrogen_pad_zero_settings"
        private const val KEY_ONLINE = "online_features_enabled"
        private const val KEY_SYNC = "sync_enabled"
        private const val KEY_PREFER_LOCAL_FIRST = "prefer_local_first"
        private const val KEY_SCAN_SUBFOLDERS = "scan_subfolders"
        private const val KEY_ALLOW_ATMOS_FALLBACK = "allow_atmos_fallback"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_FOLDER_URI = "last_folder_uri"
        private const val KEY_LAST_FOLDER_LABEL = "last_folder_label"
    }
}
