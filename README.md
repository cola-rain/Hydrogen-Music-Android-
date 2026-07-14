# Hydrogen-Music-Android-
《明日方舟》风格第三方安卓网易云音乐。
基于https://github.com/ldx123000/Hydrogen-Music进行安卓移植


本人技术有限，移植工作利用Vibe Code完成。如果您对此类项目感到厌烦或抵触，请无视本项目。

希望会有有能力的人加入开发，毕竟vibecode，产出来的东西稳定性不怎么好。

第一次开仓库如果有什么做的不好请issue


项目版本：

0. version-alpha 0.0.0 pad
预期功能：
 0. 可行性验证
 1. 适配横屏，保持原ui不变
 2. 扫描与播放本地音频
 3. 可开关的基础联网功能（账号登录、歌曲获取与播放）（因为原项目联网使用本地api增强，安卓端没有这个功能，所以先插个开关在这（gpt说的））


构建：

Android studio

环境：JDK17或内置JDK；SDK34（gpt就告诉我这些）




GPT生成的readme：
# Hydrogen Pad Zero

这是一个面向横屏平板的 **第零版** Android 工程，目标很保守：

- 先把本地音频播放跑通
- 先把触屏交互做成大按钮、大间距、无鼠标悬停依赖
- 联网功能、账号登录、网易 API、同步都保留开关和占位入口
- Dolby Atmos / HiFi 独占输出先不做硬接入

## 第一版可用内容

- 横屏优先的三栏界面
- 本地音乐文件夹选择
- 本地音频扫描
- 本地音频播放、暂停、上一首、下一首、拖动进度
- 本地搜索
- 设置面板：联网功能总开关、同步开关、优先本地、主题模式
- 联网入口保留，但默认是关闭和占位状态

## 直接编译

1. 用 Android Studio 打开这个项目根目录。
2. 等待 Gradle 同步。
3. 先确认 Android Studio 使用自带 JDK 17/Embedded JBR。
4. 点击 Run 或执行 `./gradlew assembleDebug`。

Gradle Wrapper 是推荐的构建入口，它会根据项目里的 `distributionUrl` 下载并使用对应版本的 Gradle。citeturn740158search1

## 需要注意的问题

### 1. 第一次同步慢
这是正常的。Gradle、AGP、Compose、Media3 依赖都会下载。

### 2. Wrapper 下载失败
如果网络、代理、公司防火墙、校园网限制了 `services.gradle.org`，同步会失败。处理办法：
- 换网络
- 配代理
- 手动让 Android Studio 使用已有 Gradle

### 3. JDK 版本不对
Android Gradle Plugin 8.x 通常要求较新的 JDK。最稳妥的是用 Android Studio 自带 JBR 17。

### 4. 本地音乐看不到
这是因为你还没在应用里选择音乐文件夹，或者系统没给这个文件夹持久权限。重新点“选择本地文件夹”。

### 5. 某些格式播不了
这版先把常见格式优先跑通。设备本身不支持的音频编码，或者 Dolby Atmos / E-AC3 之类格式，可能只能后续再加专门处理。

### 6. 联网功能没反应
第零版里联网功能是开关式占位。打开开关后，登录/API/同步入口会出现，但后端还没真正接到上游桌面版的那层增强服务。

### 7. 旧缓存导致同步异常
如果你改过依赖版本或同步失败很多次，先尝试：
- Android Studio `File > Invalidate Caches / Restart`
- 删除项目里的 `.gradle`、`build` 目录

### 8. 真机安装失败
确认：
- 设备允许安装调试包
- 你选的是 Debug 变体
- APK 没被系统拦截

## 这版和原桌面版的区别

桌面版依赖 Electron 的窗口、系统集成、以及本地增强 API。这个零版本先不硬搬那些桌面能力，先把平板上最重要的“播放本地音频”做稳。
