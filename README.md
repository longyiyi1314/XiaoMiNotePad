# 笔记 (NotePad)

一款面向小米平板 / 安卓平板的手写笔记应用，支持小米智能笔，可导入 PDF / PPT / Word，并通过 GitHub 私人仓库自动同步。

## 功能特性

- **手写绘画**：圆珠笔、钢笔、毛笔、马克笔、荧光笔、铅笔、橡皮擦 7 种笔型，支持自定义颜色与粗细。
- **小米智能笔**：读取触控笔压力（`PointerType.Stylus`）、开启手掌抑制（palm rejection），书写自然流畅。
- **笔记管理**：多级文件夹分类、收藏单独笔记、按标题/内容搜索。
- **导入文件**：PDF 逐页栅格化为图片可在其上手写；PPT / Word / 图片作为附件导入（系统分享或编辑器内上传）。
- **回收站**：软删除 → 回收站，可恢复或永久删除，可配置保留天数自动清理。
- **本地备份与恢复**：一键导出 / 导入 JSON 备份文件，可保存到任意位置。
- **GitHub 自动同步**：使用个人访问令牌（PAT）将笔记同步到**私人仓库**，支持周期后台同步、仅 Wi-Fi 同步、立即同步、凭据验证。冲突采用 last-write-wins（按更新时间）。
- **平板优化**：自适应栅格布局、横竖屏适配、动态取色（Android 12+）、Material 3。

## 技术栈

- Kotlin + Jetpack Compose（Material 3）
- Room（本地数据库）+ DataStore（设置）
- Hilt（依赖注入）
- WorkManager（后台周期同步）
- Retrofit / OkHttp（GitHub Contents API）
- Android `PdfRenderer`（PDF 逐页渲染）

## 项目结构

```
app/src/main/java/com/xiaominote/app/
├── NoteApp.kt                 # Application，初始化同步调度与回收站清理
├── MainActivity.kt            # Compose 入口
├── data/
│   ├── db/                    # Room: AppDatabase, DAO, Entity (Note/Folder/Attachment)
│   ├── repository/            # NoteRepository, FolderRepository（含回收站逻辑）
│   ├── prefs/                 # SettingsRepository (DataStore)
│   └── sync/                  # GithubApi, GithubSyncManager, SyncWorker, SyncScheduler
├── backup/                    # BackupManager, RecycleBinManager
├── importing/                 # DocumentImporter (PDF/PPT/Word/图片)
├── drawing/                   # Stroke, PenType, DrawingCanvas, StrokeRenderer, ColorPalette
├── di/                        # Hilt AppModule
└── ui/
    ├── theme/                 # Color / Theme / Type / AppThemeContainer
    ├── navigation/            # Route, NotePadNavGraph
    ├── screen/home/           # 首页（文件夹/收藏/搜索）
    ├── screen/editor/         # 编辑器（画布 + 笔工具栏）
    ├── screen/settings/       # 设置（GitHub 同步/主题/回收站）
    ├── screen/recyclebin/     # 回收站
    ├── screen/backup/         # 备份与恢复
    └── component/             # PenToolbar 等可复用组件
```

## 构建与运行

> 项目使用 Gradle 8.9 + AGP 8.5.2，已内置 Gradle Wrapper（`gradlew` + `gradle-wrapper.jar`）。

1. 用 **Android Studio (Hedgehog 或更新版本)** 打开 `NotePad/` 目录，IDE 会自动下载 SDK 与依赖。
2. 连接小米平板（开启 USB 调试）或启动平板模拟器。
3. 点击 **Run** 或执行 `./gradlew :app:assembleDebug` 构建 debug APK。

最低支持：**Android 8.0 (API 26)**，目标 **API 34**。

## 配置 GitHub 同步

1. 在 GitHub 创建一个**私人仓库**（例如 `my-notes`）。
2. 生成 **Personal Access Token (classic)**，勾选 `repo` 权限。
3. 打开应用 → 设置 → GitHub 自动同步，填入：令牌、仓库所有者（用户名）、仓库名称、分支（默认 `main`）。
4. 点击「保存凭据」→「验证」，验证通过后打开「启用自动同步」。
5. 笔记将以 `notes/{remoteId}.json`、`folders/{remoteId}.json` 形式存放在仓库中。

同步可在设置中调整为周期执行（最短 15 分钟）或点击「立即同步」手动触发。

## 备注

- PPT / Word 在 Android 上无内置渲染器，作为附件存储；如需在手写层上批注，建议先将文档另存为 PDF 再导入。
- 同步为 last-write-wins，适合单人多设备轻度并发；避免多端同时编辑同一笔记。
