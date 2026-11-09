package me.rerere.rikkahub.data.skills

import android.content.Context
import android.content.res.AssetManager
import java.io.File
import java.io.FileNotFoundException

/**
 * 随 APK 发布的默认技能：以 `assets/default_skills/<name>/` 打包，目录结构与普通技能相同（SKILL.md + 附属文件）。
 *
 * 首次启动时播种到技能目录 `filesDir/skills/`，之后它们就是普通技能，可编辑、可删除。
 * 播种只补齐缺失的技能，不会覆盖用户已有的修改，也不会恢复用户已删除的技能。
 *
 * 注意：aapt 默认会忽略以 `.` 开头的文件和以 `_` 开头的目录，不要用这类名称。
 */
internal object DefaultSkills {
    private const val ASSETS_ROOT = "default_skills"
    private const val SEED_MARK = ".seeded"
    private const val LEGACY_BUILTIN_DIR = "builtin_skills"

    /**
     * 将 assets 中的默认技能播种到 [skillsDir]，完成后写入 [SEED_MARK] 标记，
     * 之后的启动不再播种，避免把用户删除的技能恢复回来。
     */
    fun seedIfNeeded(context: Context, skillsDir: File) {
        val seedMark = skillsDir.resolve(SEED_MARK)
        if (seedMark.exists()) return

        val assets = context.assets
        assets.list(ASSETS_ROOT).orEmpty().forEach { name ->
            val target = skillsDir.resolve(name)
            if (target.exists()) return@forEach
            // 先复制到隐藏的临时目录再 rename，避免中途失败留下半个技能目录
            val staging = skillsDir.resolve(".$name.staging.tmp")
            staging.deleteRecursively()
            staging.mkdirs()
            assets.copyTree("$ASSETS_ROOT/$name", staging)
            if (!staging.renameTo(target)) {
                staging.deleteRecursively()
            }
        }
        seedMark.writeText("seeded")

        // 旧版本把默认技能解压到独立的 builtin_skills 目录，内容已随播种回到技能目录，清理残留
        context.filesDir.resolve(LEGACY_BUILTIN_DIR).deleteRecursively()
    }

    private fun AssetManager.copyTree(assetPath: String, target: File) {
        val children = list(assetPath).orEmpty()
        if (children.isNotEmpty()) {
            target.mkdirs()
            children.forEach { copyTree("$assetPath/$it", target.resolve(it)) }
            return
        }
        // AssetManager 无法区分文件和空目录：list 为空时先按文件打开，打不开则视为空目录
        try {
            open(assetPath).use { input ->
                target.parentFile?.mkdirs()
                target.outputStream().use { input.copyTo(it) }
            }
        } catch (_: FileNotFoundException) {
            target.mkdirs()
            return
        }
        // assets 不保留可执行位，带 shebang 的脚本需要手动恢复，才能在 workspace 中直接 ./script 执行
        if (target.startsWithShebang()) {
            target.setExecutable(true, false)
        }
    }

    private fun File.startsWithShebang(): Boolean = inputStream().use { input ->
        input.read() == '#'.code && input.read() == '!'.code
    }
}
