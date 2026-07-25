CHINESE
# Hydrogen Music Pad

Hydrogen Music Pad 是 Hydrogen 桌面播放器首个 Android 移植版本的框架。  
本代码库采用保守设计策略：首先专注于 **平板横屏模式**、**本地音频播放** 以及 **触控友好型界面**。

## 本版本的功能

- 优先支持横屏/平板模式的界面
- 本地音乐文件夹选择与扫描
- 通过 Media3 / ExoPlayer 进行本地音频播放
- 基本播放控制：播放、暂停、下一首、上一首、快进/快退、音量调节
- 触控友好的列表卡片及更大的点击目标
- 在线功能目前仅作为开关/占位符保留，供日后实现：
  - 登录
  - 同步
  - 网易 API 桥接
  - 在线搜索
- 杜比全景声（Dolby Atmos）/HiFi 后端功能暂缓实现

## 构建要求

本地开发：

- Android Studio（最新稳定版）
- 与 `compileSdk 34` 匹配的 Android SDK
- GitHub Actions 工作流使用 JDK 21
- 若进行本地构建，通常使用 Android Studio 内置的 JBR/JDK 是最稳妥的选择

云端构建：

- GitHub Actions
- 提供的的工作流会构建一个调试版 APK 并将其上传为构建产物

## 使用 Android Studio 构建

1. 在 Android Studio 中打开仓库根目录。
2. 等待 Gradle 同步完成。
3. 让 Android Studio 下载缺失的 SDK / 库依赖项。
4. 点击 **运行** 或使用 **构建 > 构建 APK**。

## 使用 GitHub Actions 构建

1. 将项目推送到 GitHub。
2. 打开仓库的 **Actions** 选项卡。
3. 运行名为 **Build Android APK** 的工作流。
4. 任务完成后，下载已上传的调试版 APK 构建产物。

## 当前功能状态

### 已实现
- 使用系统文件夹选择器选择本地文件夹
- 扫描支持的本地音频文件
- 以触控友好的列表形式显示歌曲
- 播放 / 暂停 / 上一首 / 下一首
- 快进/快退和音量控制
- 主题模式切换
- 本地优先播放和扫描行为的设置

### 后续计划
- 真正的网易登录桥接
- 真正的网易 API 调用
- 库/用户状态同步
- MPV HiFi 后端
- 杜比/Atmos 特殊处理
- 原应用中仅限桌面端的功能

## 文件结构说明

为确保早期移植阶段的稳定性，代码目前保留了原始的 Java 包命名空间。仓库名称/根目录及应用标签为便于在 GitHub 上发布，特意设置得更为通用。

## 常见问题

### 首次 Gradle 同步速度较慢
这是正常现象。Gradle、Android Gradle 插件、Compose 以及 Media3 依赖项需要下载一次。

### 因 Java 版本不匹配导致构建失败
在 GitHub Actions 中请使用 Java 21。本地开发时，如条件允许，请使用 Android Studio 内置的 JDK。

### Android SDK 错误
如果 Gradle 提示缺少平台或构建工具包，请在 Android Studio 的 SDK 管理器中安装所需的 SDK 版本。

### 本地音乐未显示
应用仅会扫描您明确选定并授予访问权限的文件夹。

### 部分音频格式无法播放
支持的播放格式仍取决于设备和 Android 的内置解码器。高度专业的格式可能需要后续的原生处理。

### 在线按钮无反应
此阶段出现此情况属正常现象。这些按钮目前仅作为开关和占位符存在，以便 UI 和设置布局为后续集成做好准备。

## 贡献者须知

请将首要目标保持简单：在添加复杂的在线或原生播放功能之前，确保应用能够稳定地构建、启动并可靠地播放本地音频。









ENGLISH
# Hydrogen Music Pad

Hydrogen Music Pad is a first-pass Android port scaffold for the Hydrogen desktop player.  
This repository is intentionally conservative: it focuses on **landscape tablet use**, **local audio playback**, and **touch-friendly UI** first.

## What this version is for

- Landscape / tablet-first interface
- Local music folder picking and scanning
- Local audio playback with Media3 / ExoPlayer
- Basic playback controls: play, pause, next, previous, seek, volume
- Touch-friendly list cards and larger tap targets
- Online features kept as switches/placeholders for later wiring:
  - login
  - sync
  - NetEase API bridge
  - online search
- Dolby Atmos / HiFi backend is deferred for now

## Build requirements

For local development:

- Android Studio (a recent stable version)
- The Android SDK matching `compileSdk 34`
- JDK 21 is used by the GitHub Actions workflow
- If you build locally, Android Studio's embedded JBR/JDK is usually the safest choice

For cloud builds:

- GitHub Actions
- The provided workflow builds a debug APK and uploads it as an artifact

## Build with Android Studio

1. Open the repository root in Android Studio.
2. Wait for Gradle sync to finish.
3. Let Android Studio download the missing SDK / library dependencies.
4. Click **Run** or use **Build > Build APK**.

## Build with GitHub Actions

1. Push the project to GitHub.
2. Open the repository's **Actions** tab.
3. Run the workflow named **Build Android APK**.
4. Download the uploaded debug APK artifact after the job finishes.

## Current feature status

### Working now
- Select a local folder with the system folder picker
- Scan supported local audio files
- Show songs in a touch-friendly list
- Play / pause / previous / next
- Seek and volume control
- Theme mode switch
- Settings for local-first playback and scan behavior

### Reserved for later
- Real NetEase login bridge
- Real NetEase API calls
- Sync of library / user state
- MPV HiFi backend
- Dolby / Atmos special handling
- Desktop-only features from the original app

## File layout notes

The code currently keeps the original Java package namespace for stability during the early porting stage. The repository name / root folder and app label are intentionally more generic for GitHub publishing.

## Common problems

### Gradle sync is slow the first time
This is normal. Gradle, Android Gradle Plugin, Compose, and Media3 dependencies must be downloaded once.

### Build fails with a Java version mismatch
Use Java 21 in GitHub Actions. For local development, use the Android Studio embedded JDK if possible.

### Android SDK errors
If Gradle says a platform or build-tools package is missing, install the requested SDK version in Android Studio's SDK Manager.

### Local music does not appear
The app only scans folders that you explicitly pick and grant access to.

### Some audio formats do not play
Supported playback still depends on the device and Android's built-in decoders. Highly specialized formats may need later native handling.

### Online buttons do nothing
That is expected in this stage. They are present as switches and placeholders so the UI and settings layout are ready for later integration.

## Notes for contributors

Please keep the first goal simple: get the app building, opening, and playing local audio reliably before adding heavy online or native playback features.
