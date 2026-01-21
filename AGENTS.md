# Repository Guidelines

## Project Structure & Modules
- `app/`: Android app (Jetpack Compose UI, DI, data, Room, navigation).
- `ai/`: AI SDK and provider integrations (OpenAI, Google, Anthropic) with optional native/NN deps.
- `highlight/`, `search/`, `tts/`, `common/`: Feature and utility modules.
- Tests: `ai/src/test` (unit), `ai/src/androidTest` (instrumented); `app/src/test` scaffolded.
- Assets: `app/src/main/assets`, resources under `app/src/main/res`.

## Architecture & Key Concepts
- Detailed reference: `CLAUDE.md` and `locale-tui/CLAUDE.md`.
- **Assistant**: Assistant configuration (system prompt, model params, headers/tools/memory/transformers). (`app/src/main/java/me/rerere/rikkahub/data/model/Assistant.kt`)
- **Conversation / MessageNode**: Persistent thread with message branching via a tree of nodes. (`app/src/main/java/me/rerere/rikkahub/data/model/Conversation.kt`)
- **UIMessage**: Provider-agnostic message abstraction supporting mixed content parts + streaming merges. (`ai/src/main/java/me/rerere/ai/ui/Message.kt`)
- **Message Transformers**: Pre-/post-processing pipeline (templates, `<think>`, regex, docs-as-prompt, OCR, etc.). (`app/src/main/java/me/rerere/rikkahub/data/ai/transformers/Transformer.kt`)

## UI Development
- Follow Material Design 3 and prefer reusing existing composables from `app/src/main/java/me/rerere/rikkahub/ui/components/`.
- Page layout patterns: reference `SettingProviderPage.kt`; use `FormItem` for consistent form rows.
- Icons: use `Lucide.XXX` and import `import com.composables.icons.lucide.XXX` for each icon.
- Toasts: use `LocalToaster.current`.

## Strings (Chinese-only)
- This fork keeps a single Chinese string set in default `values/strings.xml` (no `values-xx` locales).
- String resources: `app/src/main/res/values/strings.xml` and `search/src/main/res/values/strings.xml`; Compose uses `stringResource(R.string.key_name)`.
- Key naming: page-specific keys should use a page prefix (e.g., `setting_page_`).
- APK locale pruning: `app/build.gradle.kts` uses `androidResources.localeFilters += listOf("zh")` to strip other locales (including dependencies).

## Database
- Room database with migration support; schema files in `app/schemas/`.
- Database version tracked in `AppDatabase.kt`; keep migrations forward-only and compatible.

## AI Provider Integration
- New providers: `ai/src/main/java/me/rerere/ai/provider/providers/` (extend base `Provider`, follow existing patterns).
- Streaming responses use SSE (OkHttp); changes should preserve existing streaming behaviors.

## Coding Style & Naming
- Kotlin with 4‑space indent, 120 char line limit (`.editorconfig`).
- Classes/objects: PascalCase; functions/properties: camelCase; resources: snake_case.
- Compose: Composables start UpperCamelCase; pages typically end with `Page` (e.g., `SettingProviderPage`).
- Keep modules isolated; share utilities via `common`.

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX test/espresso (instrumented).
- Place unit tests alongside module under `src/test/...`; instrumented under `src/androidTest/...`.
- Name tests `*Test.kt` and cover parsing, providers, and critical transforms.
- Validate instrumented flows for streaming/SSE where feasible.

## Commit & PR Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `chore:`, `refactor:` … with a clear, concise subject. Scope optional.
- Keep PRs focused; include description, linked issue, and screenshots for UI changes.
- Run `test` and `lint` before opening PRs; note any platform caveats (device/emulator).
- Per README: do not submit new languages, unrelated large features, or broad refactors/AI‑generated mass edits.

## Security & Configuration
- Never commit secrets or signing files. Keep API keys in secure storage; avoid hardcoding.
- `local.properties` holds signing values; `google-services.json` stays in `app/` and is ignored by Git.
