# Changelog

本项目的所有显著变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added

- 新增 Phone Use（仅 GitHub 渠道、Android 11+）：AI 通过无障碍服务读取屏幕元素、截图、点击、滑动与输入，坐标统一为 0-1000 网格；在助手的本地工具中开关，开启前要求后台悬浮窗已开启，悬浮窗在截图与执行手势时让出屏幕并显示当前动作；本应用在前台时不操作，支付与银行类应用可见时不可读取和操作；Play 渠道不声明该无障碍服务
- 新增按上下文占用比例的自动压缩：回复结束后，或一轮回复中途工具调用之间，上下文达到聊天模型窗口的设定比例时自动把历史压缩成摘要继续，摘要后的检查点带上续写提示；设置-通用设置可开关并调整触发比例（50%–95%）
- 设置首页在未授予通知权限时显示通知权限提示卡片，原本地网络权限卡片抽出为通用的权限请求卡片复用
- 信任 Android 用户自行安装的 CA 证书（`network_security_config`），便于抓包与自签网关调试
- 调试页新增创建约 500k token 对话的按钮，用于测试上下文压缩
- 聊天消息中的 Phone Use 工具卡片按动作显示标题与图标（读取屏幕、截图、点击、长按、滑动、文本输入等）
- 可用模型面板支持手动刷新：面板右上角新增刷新按钮，远端模型列表请求以提供商配置为 key（增删、排序已添加的模型不重复请求），加载中显示进度条，失败时保留上次成功列表并显示错误提示

### Changed

- 模型支持独立的最大输出上限（token）：models.dev 数据补充 `limit.output` 并重新生成内置快照，添加或切换模型时自动回填，也可在提供商页的模型表单中修改；对话请求优先使用助手设置的「最大Token数」，助手未设置时使用模型的上限，压缩摘要请求仍不指定输出上限
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
- 压缩机制对齐 SillyTavern（酒馆）摘要形态：压缩提示词 1:1 采用其默认摘要提示词（自由文本摘要、以上一份摘要为基础增量扩展），压缩对话框新增摘要词数上限设置（25–1000、步进 25、默认 200 与酒馆一致）；压缩范围对齐酒馆策略，手动压缩总是保留最后一条消息，上下文放不下压缩请求时按 token 预算（预留 64）丢弃较新的消息、留给下次增量压缩
- 快捷消息短按追加到输入框末尾（已有内容且不以换行结尾时自动补换行），长按替换输入框全部内容
- Phone Use 无障碍服务关闭提示补充开启自启动的建议
- 版本升级至 3.2.0（versionCode 196）

### Fixed

- 修复 Claude 系列 400 报错（共 5 项）：相邻签名思考块被拼接导致签名绑定校验失败（非流式结果的思考块各自保留、不再拼接）；流式 `signature_delta` 覆盖而非累加（改为拼接增量片段）；`redacted_thinking` 块被丢弃（非流式与流式均保留加密数据并原样回传）；thinking 参数与模型世代不符（4.6+ 用 `adaptive`、≤4.5 用 `enabled`+`budget_tokens`、OFF 省略字段）；采样参数 `temperature`/`top_p` 在 4.6+ 上被拒（仅对旧模型且思考关闭时发送）；`max_tokens` 超出旧模型上限（按 modelId clamp 到 4096/8192/64000）；图片编码失败时生成空 text 块违反 `minLength:1`（改为快速失败抛出异常）；启用 thinking 时加 `drop_block` 兜底避免前缀变化触发 400
- 修复 Claude / OpenRouter 链路 400 与 403 报错（共 5 项）：OpenRouter 链路纯音视频/文档附件消息被序列化成空 `content` 数组触发 `invalid_prompt`（无可发送 Text/Image 时改用文字说明占位）；Claude 图片 `source.data` 收到非合法 base64（`http(s)` URL 改用 `type:"url"` source 直传，`data:` URL 解析出纯 base64 段与真实 mime 再填入）；OpenRouter guardrail 403 错误未提示命中原因（`parseErrorDetail` 提取 `metadata.patterns` 并附到错误信息，错误卡片展示命中注入检测模式）；OpenRouter 对 Claude 系模型拒绝 `reasoning.effort:"none"`（Claude OFF 档省略 `reasoning` 参数，思考为 opt-in 即不启用）；AT_DEPTH 注入深度溢出时插到 system 之前破坏消息序（溢出 clamp 到 system 之后，注入深度 UI 限制最小为 1）
- 修复 Responses API 链路 400 报错（共 7 项）：`include` 强塞 `reasoning.encrypted_content` 被枚举不含该值的第三方网关拒绝（该值已是 legacy，移除并下线只服务它的能力位）；input message item 缺 `type:"message"`、单段文本压成裸字符串、system/developer 的 part 被写成 `output_text`（统一补 `type`、按 role 分派 part 类型、content 统一数组）；历史 TOOL 消息发出非法 `role:"tool"`（构建阶段按 user 文本回放）；回放的 reasoning item 带服务端 `id` 与明文 `content`（`store:false` 上游不认，改为仅回放 `encrypted_content`，无密文时不回放）；`input_image` 缺 `detail`（消息 content 与 `function_call_output.output` 统一补 `auto`）；`max_output_tokens` 可小于 16 且被采样参数开关连带丢弃（下限 clamp 到 16 并移出采样分支）；推理模型仍收到 `temperature`/`top_p`（改按 `ModelAbility.REASONING` 门控）
- 修复每日构建签名校验失败：release 签名的 `storeFile` 改为按仓库根目录解析，与 CI 写入密钥的 `app/app.key` 路径对齐
- 修复后台 shell 前台服务被系统回收：前台服务类型由 `dataSync` 改为 `specialUse` 并声明 subtype，避免 Android 15+ 的 24 小时 6 小时额度与其他 dataSync 服务共享
- 修复 Web 端消息统计的输入 token 与 App 端上下文占用不一致：多步工具调用时改为显示最后一次请求的输入与缓存值，输出与速度仍按累计值计算，多步时显示步数，累计输入放在悬停提示里
- 修复长对话的对话地图（概览）滑动卡顿：卡片正文显示前先截断到 300 字符（搜索仍匹配全文），正则清洗移到后台线程，生成中只计算最新的节点列表，列表 key 改为随卡片保存
- 修复 Mistral 推理模型多轮对话报 422：历史 assistant 消息的思考内容按 Mistral 的 content thinking 块格式回传（不再发 `reasoning_content` 字段），响应与流式 `content` 中的 thinking 块解析统一抽出 `parseChatCompletionsContent` 处理并遍历所有块

- 补充 Room `AutoMigration(32, 33)` 的 `@DeleteTable` spec，修复 KSP 编译失败
- 修复 `ChatMessageActions.kt` 中缺失的闭合括号
- 通过 pnpm overrides 强制传递依赖版本，消除 web-ui 的 3 条 Dependabot 安全告警：katex 统一为 0.18.10（CVE-2026-103923），tinypool 升至 2.2.0（CVE-2026-104848/104849）
