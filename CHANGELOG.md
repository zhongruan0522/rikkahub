# Changelog

本项目的所有显著变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Changed

- 设置主页「通用设置」中的颜色模式改为「主题设置」子页：下拉选择主题配色（默认、Claude、T3 Chat、Mono、泡泡糖，与 Web UI 配色对齐，新增 4 套预设主题）和颜色模式（浅色、深色、跟随系统）
- 移除内置渠道 AiHubMix、APIMart、302.AI、随想AI网关、MaruCode、STA1N API 及相关资源（图标、请求头特例、TTS 预设）
- 移除提供商页面右上角的推荐提供商入口及推荐列表
- 应用安装包名（applicationId）由 `me.rerere.rikkahub` 改为 `rikka.hub`
- 切换类按钮（`ToggleButton` / `IconToggleButton`）选中后只变色，保持原有形状，不再做方角/圆角切换动画
- web-ui 前端依赖升级至范围内最新版本：react-router 7.18.5、react 19.3.1、radix-ui 1.8.1、tailwindcss 4.3.5 等，并同步刷新 browserslist、js-yaml、baseline-browser-mapping 等传递依赖
- 每日构建（Latest Build 工作流）同时产出 GitHub 渠道的 debug 与 release APK，一并上传到 Latest Prerelease

### Fixed

- 修复每日构建签名校验失败：release 签名的 `storeFile` 改为按仓库根目录解析，与 CI 写入密钥的 `app/app.key` 路径对齐

- 补充 Room `AutoMigration(32, 33)` 的 `@DeleteTable` spec，修复 KSP 编译失败
- 修复 `ChatMessageActions.kt` 中缺失的闭合括号
- 通过 pnpm overrides 强制传递依赖版本，消除 web-ui 的 3 条 Dependabot 安全告警：katex 统一为 0.18.10（CVE-2026-103923），tinypool 升至 2.2.0（CVE-2026-104848/104849）
