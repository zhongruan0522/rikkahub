# Changelog

本项目的所有显著变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Changed

- 应用安装包名（applicationId）由 `me.rerere.rikkahub` 改为 `rikka.hub`
- 切换类按钮（`ToggleButton` / `IconToggleButton`）选中后只变色，保持原有形状，不再做方角/圆角切换动画

### Fixed

- 补充 Room `AutoMigration(32, 33)` 的 `@DeleteTable` spec，修复 KSP 编译失败
- 修复 `ChatMessageActions.kt` 中缺失的闭合括号
