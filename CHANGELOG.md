# Changelog

本项目的所有显著变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Changed

- 移除 ChatGPT 订阅与 Grok 订阅（账号登录型）提供商：新增提供商列表不再提供这两项，登录、token 刷新、订阅用量展示及相关 OAuth 协议实现一并下线；旧数据中的 ChatGPT/Grok 提供商在读取时自动清理，避免设置反序列化失败
- 优化设置-请求日志（Logs）：请求记录开关默认开启；新增「详细模式」开关，关闭时仅记录失败的请求，开启后记录所有请求的标头、正文与元数据；日志列表只显示简短错误摘要，堆栈等长内容点击条目在详情中查看；每条日志新增复制按钮，一键复制整条记录数据便于反馈
- 设置主页「通用设置」新增「主题设置」子页并整合原偏好设置「主题」页：主题配色下拉（默认、Claude、T3 Chat、Mono、泡泡糖，与 Web UI 配色对齐，新增 4 套预设主题）、颜色模式下拉（浅色、深色、跟随系统）、动态颜色与 AMOLED 纯黑开关、主题管理入口；选中预设配色时自动关闭动态颜色，偏好设置中不再有重复入口
- 移除内置渠道 AiHubMix、APIMart、302.AI、随想AI网关、MaruCode、STA1N API 及相关资源（图标、请求头特例、TTS 预设）
- 移除提供商页面右上角的推荐提供商入口及推荐列表
- 应用安装包名（applicationId）由 `me.rerere.rikkahub` 改为 `rikka.hub`
- 切换类按钮（`ToggleButton` / `IconToggleButton`）选中后只变色，保持原有形状，不再做方角/圆角切换动画
- web-ui 前端依赖升级至范围内最新版本：react-router 7.18.5、react 19.3.1、radix-ui 1.8.1、tailwindcss 4.3.5 等，并同步刷新 browserslist、js-yaml、baseline-browser-mapping 等传递依赖
- 每日构建（Latest Build 工作流）同时产出 GitHub 渠道的 debug 与 release APK，一并上传到 Latest Prerelease
- 每日构建拆分 debug / release 两个并行 job，由 publish job 汇总产物发布；所用 actions 升级至最新主版本（checkout v7、setup-java v6、setup-node v7、pnpm/action-setup v6、gh-release v3 等），Gradle 缓存改用 `gradle/actions/setup-gradle` 并启用 build cache 提速

### Fixed

- 渠道 LOGO 名称识别不出时不再显示文字头像，统一改用渠道类型对应的图标（OpenAI / Google / Claude / ChatGPT / Grok）
- 修复每日构建签名校验失败：release 签名的 `storeFile` 改为按仓库根目录解析，与 CI 写入密钥的 `app/app.key` 路径对齐

- 补充 Room `AutoMigration(32, 33)` 的 `@DeleteTable` spec，修复 KSP 编译失败
- 修复 `ChatMessageActions.kt` 中缺失的闭合括号
- 通过 pnpm overrides 强制传递依赖版本，消除 web-ui 的 3 条 Dependabot 安全告警：katex 统一为 0.18.10（CVE-2026-103923），tinypool 升至 2.2.0（CVE-2026-104848/104849）
