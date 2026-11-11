# Repository Guidelines

## Project Overview

RikkaHub is a native Android LLM chat client that supports switching between different AI providers
for conversations.
Built with Jetpack Compose, Kotlin, and follows Material 3 Expressive (MD3 Expressive) design language.

## Design Language

The UI follows Material 3 Expressive. New and reworked screens should match it rather than the older baseline
Material 3 look (cards with dividers, plain forms).

- The app theme is `MaterialExpressiveTheme` (app/src/main/java/me/rerere/rikkahub/ui/theme/Theme.kt); prefer the
  expressive variants of components, shapes (`MaterialShapes`), emphasized typography (e.g. `titleSmallEmphasized`)
  and motion from `androidx.compose.material3` over hand-rolled equivalents.
- Settings-style and form pages use segmented groups via `CardGroup` (
  app/src/main/java/me/rerere/rikkahub/ui/components/ui/CardGroup.kt) instead of a card with dividers.
- Favor larger corner radii, filled/tonal containers, and whole-row click targets.

## Build, Test, and Development Commands

```bash
./gradlew assembleGithubDebug    # Build the Debug APK for the GitHub channel
./gradlew assembleGithubRelease  # Build the APK for GitHub Release / daily builds
./gradlew bundlePlayRelease      # Build the AAB for Google Play
./gradlew test                   # Run JVM unit tests for all modules
./gradlew lint                   # Run Android Lint
./gradlew spotlessApply          # Format Kotlin files changed relative to origin/master with ktlint
./gradlew spotlessCheck          # Check formatting only without modifying files
```

The app has a `distribution` product flavor dimension: `github` (default) and `play`, sharing the same applicationId.

## Module Structure

- **app**: Main application module with UI, ViewModels, and core logic
- **ai**: AI SDK abstraction layer for different providers (OpenAI, Google, Anthropic)
- **common**: Common utilities and extensions
- **document**: Document parsing module for handling PDF, DOCX, PPTX, and EPUB files
- **material3**: Material color utility extensions used by the app UI
- **ui**: Reusable Compose UI components that do not depend on app logic
- **search**: Search functionality SDK for multiple providers (Exa, Tavily, Zhipu, Bing, Brave, SearXNG, and others)
- **speech**: Speech module for TTS and ASR implementations
- **web**: Embedded web server module that provides Ktor server startup function and hosts static frontend build files (
  built from web-ui/ React project)
- **workspace**: Sandboxed per-workspace file system and shell execution environment exposed to the AI as tools.
- **terminal**: Terminal emulator core taken from termux-app's `terminal-emulator` (package `com.termux.terminal`, kept
  binary compatible with the `terminal-view` aar). `TerminalSession` runs over a `TerminalTransport`: local PRoot
  terminals use `LocalPtyTransport`, SSH workspaces use the app's `SshTerminalTransport`. Origin and local changes are
  listed in terminal/README.md.

## Concepts

- **Assistant**: An assistant configuration with system prompts, model parameters, and conversation isolation. Each
  assistant maintains its own settings including temperature, context size, custom headers, tools, memory options, regex
  transformations, and prompt injections (mode/lorebook). Assistants provide isolated chat environments with specific
  behaviors and capabilities. (app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt)

- **Conversation**: A persistent conversation thread between the user and an assistant. Each conversation maintains a
  list of MessageNodes in a tree structure to support message branching, along with metadata like title, creation time,
  update time, pin status, chat suggestions, optional conversation-level system prompt, and prompt injection bindings.
  Once a conversation is persisted it also holds a `ConversationConfig` snapshot (chat model, reasoning level, search,
  MCP servers, workspace, skills) taken from the assistant; from then on chat-page changes to those settings stay on
  the conversation, and code should read them through `Settings.getAssistantOf(conversation)` /
  `Settings.getChatModelOf(conversation)` instead of the assistant directly. Once loaded, the in-memory
  `ConversationSession` is the single source of truth: change a conversation through `ChatService` (which applies
  `session.update { latest -> ... }` and then `flush()`), never by saving a whole `Conversation` snapshot or one read
  back from the database; deleting also goes through `ChatService` so a running generation cannot re-insert it. (
  app/src/main/java/me/rerere/rikkahub/data/model/Conversation.kt,
  app/src/main/java/me/rerere/rikkahub/data/model/ConversationConfig.kt,
  app/src/main/java/me/rerere/rikkahub/service/ConversationSession.kt)

- **UIMessage**: A platform-agnostic message abstraction that encapsulates chat messages with different types of content
  parts (text, images, documents, reasoning, tool calls/results, etc.). Each message has a role (USER, ASSISTANT,
  SYSTEM, TOOL), creation timestamp, model ID, token usage information, and optional annotations. UIMessages support
  streaming updates through chunk merging. (ai/src/main/java/me/rerere/ai/ui/Message.kt)

- **MessageNode**: A container holding one or more UIMessages to implement message branching functionality. Each node
  maintains a list of alternative messages and tracks which message is currently selected (selectIndex). This enables
  users to regenerate responses and switch between different conversation branches, creating a tree-like conversation
  structure. (app/src/main/java/me/rerere/rikkahub/data/model/Conversation.kt)

- **Message Transformer**: A pipeline mechanism for transforming messages before sending to AI providers (
  InputMessageTransformer) or after receiving responses (OutputMessageTransformer). Transformers can modify message
  content, add metadata, apply templates, handle special tags, convert formats, and perform OCR. Common transformers
  include:
  - TemplateTransformer: Apply Pebble templates to user messages with variables like time/date
  - ThinkTagTransformer: Extract `<think>` tags and convert to reasoning parts
  - RegexOutputTransformer: Apply regex replacements to assistant responses
  - DocumentAsPromptTransformer: Convert document attachments to text prompts
  - Base64ImageToLocalFileTransformer: Convert base64 images to local file references
  - OcrTransformer: Perform OCR on images to extract text

  Output transformers support `visualTransform()` for UI display during streaming and `onGenerationFinish()` for final
  processing after generation completes.
  (app/src/main/java/me/rerere/rikkahub/data/ai/transformers/Transformer.kt)

## Internationalization

- String resources are usually located in `app/src/main/res/values*/strings.xml`; feature modules such as `search`
  may also maintain their own `values*/strings.xml`
- Use `stringResource(R.string.key_name)` in Compose
- Page-specific strings should use page prefix (e.g., `setting_page_`)
- If the user does not explicitly request localization, prioritize implementing functionality without considering
  localization. (e.g `Text("Hello world")`)
- Add, translate or clean up strings with the `tools/i18n` CLI (Bun); see the `i18n-localization` skill.

## Code Style: Match the Upstream Handwritten Style

This repository is forked from rikkahub/rikkahub, which serves as the style baseline. When writing new code or resolving conflicts while syncing upstream, converge toward whichever side is closer to upstream; do not keep verbose AI-generated variants.

- Write comments only for non-obvious whys: protocol pitfalls, edge cases, counter-intuitive behavioral assumptions — one sentence each. No `/** API doc */`-style KDoc on private functions, no line-by-line narration of what the code does.
- Don't invent an infrastructure layer for a single problem: handle compatibility branches directly with `when` on host/modelId instead of adding a whole set of helper abstractions such as enums, private constants, and extension functions.
- Prefer direct expressions: favor `when` mappings and chained calls over defensive intermediate variables like `effectiveXxx`.
- One commit per change: the title says one thing in one sentence; don't cram multiple fixes into one commit with a parenthesized checklist in the title.
- When writing new features, prefer imitating the existing structure and style of similar upstream files; the more specific the reference, the easier the style converges (e.g. AnySearchService written after SearchService kept the upstream style).

Contrast sample of perceived divergence: upstream e1667163 and this repo's f232ebb7 fix the same kind of Claude thinking-block signature issue — the former has only 1 comment and zero new infrastructure, while the latter is comment-heavy and introduces an infrastructure layer of enums + multiple extension functions + constants.

## Behavioral Anti-Pattern Warning: No Correction Residue

Strictly forbid the following behavior pattern of "leaving historical traces and self-justification after correction":

> The user asks for a plate of "scrambled eggs with tomatoes"; the agent secretly adds "Dongpo pork". After being called out it removes it, but the commit/PR says "scrambled eggs with tomatoes (no Dongpo pork)" and the comments explain at length why the dish does not need Dongpo pork.

**Delivery requirement**: Any code, comments, documentation, commit messages, and replies must directly present the aligned **clean final-state design**, and must never contain any prior mistakes, self-justification, or correction traces such as "removed X logic here".

## Commit Conventions

Use Conventional Commits (`feat:` / `fix:` / `docs:` …), write descriptions in Chinese, one commit per fix, and never combine multiple tasks into one commit.

Any code generated or modified with AI assistance must note the tool environment and model used at the **end of the commit message** (and in the corresponding **PR description**) in the following format, one line per model/tool when multiple are involved:

```
assisted-by：{agent_name}：{model}
```

- `{agent_name}`: the AI coding tool/environment used, e.g. `opencode`, `codex`, `cursor`
- `{model}`: the actual model used (including provider, format like `provider/model`), e.g. `Zhipu/GLM-5.3[Max]`

Example:

```bash
git commit -m "feat(channel): support batch model pulling

assisted-by：claude-code：Zhipu/GLM-5.3[Max]"
```
