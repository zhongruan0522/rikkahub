<div align="center">
  <img src="docs/icon.png" alt="App 图标" width="100" />
  <h1 >RikkaHub</h1>
  <p>一个原生Android LLM 聊天客户端，支持切换不同的供应商进行聊天</p>
</div>

> 本项目并非官方版本，请仔细鉴别,需要官方原版请前往[Rikkahub](https://github.com/rikkahub/rikkahub)
>
> 本项目主要做来如下修改
>
> 1. 优化了OpenAI、Google、Anthropic等请求，使其更遵循官方规范
> 2. 移除了所谓的赞助商、AI翻译
> 3. 修复了内部服务不可移除掉的逻辑
> 4. 增加了更多的内置官方提供商（不包含中转站）、搜索渠道（anysearch）
> 5. 丰富主题样式

## ✨ 功能特色

- 🎨 现代化安卓APP设计（Material 3 Expressive / 预测性返回）和 🌙 暗色模式
- 📦 工作区：基于 proot 的 Linux 智能体环境
- 🎬 媒体生成（图片、视频）
- 🖥️ Web多端访问支持
- 🛠️ MCP 与 Skills 支持
- 🔄 多种类型的供应商支持，自定义 API / URL / 模型（目前支持 OpenAI、Google、Anthropic）
- 🖼️ 多模态输入支持
- 📝 Markdown 渲染（支持代码高亮、数学公式、表格、Mermaid）
- 🔍 搜索功能（Exa、Tavily、Zhipu、LinkUp、Brave、Perplexity、..）
- 🧩 Prompt 变量（模型名称、时间等）
- 🤳 二维码导出和导入提供商
- 🤖 智能体自定义
- 🧠 基于文件的记忆
- 🌐 自定义HTTP请求头和请求体

## ✨ 开发

本项目使用[Android Studio](https://developer.android.com/studio)开发。

技术栈文档:

- [Kotlin](https://kotlinlang.org/) (开发语言)
- [Koin](https://insert-koin.io/) (依赖注入)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI 框架)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore?hl=zh-cn#preferences-datastore) (
  偏好数据存储)
- [Room](https://developer.android.com/training/data-storage/room) (数据库)
- [Coil](https://coil-kt.github.io/coil/) (图片加载)
- [Material 3 Expressive](https://m3.material.io/) (UI 设计)
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) (导航)
- [Okhttp](https://square.github.io/okhttp/) (HTTP 客户端)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) (Json序列化)

## 📄 许可证

本项目基于 [GNU Affero General Public License v3.0](LICENSE) (AGPL-3.0) 开源。
