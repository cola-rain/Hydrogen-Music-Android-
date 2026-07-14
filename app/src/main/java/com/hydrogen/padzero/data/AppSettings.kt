
package com.hydrogen.padzero.data

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppSettings(
    val onlineFeaturesEnabled: Boolean = false,
    val syncEnabled: Boolean = false,
    val preferLocalFirst: Boolean = true,
    val scanSubfolders: Boolean = true,
    val allowAtmosFallback: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastFolderUri: String? = null,
    val lastFolderLabel: String = "尚未选择本地音乐文件夹",
)
