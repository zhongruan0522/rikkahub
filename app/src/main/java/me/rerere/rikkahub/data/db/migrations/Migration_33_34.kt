package me.rerere.rikkahub.data.db.migrations

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.rerere.rikkahub.data.db.DatabaseMigrationTracker

private const val TAG = "Migration_33_34"

/**
 * message_node 的索引从 (conversation_id) 换成 (conversation_id, node_index)，workspaces 新增 storage_mounts 列。
 *
 * 本项目的 33 用于删除翻译历史表，上游 33 的复合索引和 34 的共享存储挂载列合并到这一次迁移，迁移后与上游 34 的表结构对齐。
 * 加载会话时按 node_index 排序分页读取，只有 conversation_id 索引时每一页都要把整个会话的行取出来排序，
 * 耗时随消息数平方增长；手写迁移只建索引、删索引、加列，避免自动迁移重建整张 message_node 表。
 */
val Migration_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i(TAG, "migrate: start migrate from 33 to 34 (message_node index, workspace storage mounts)")
        DatabaseMigrationTracker.onMigrationStart(33, 34)
        try {
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_message_node_conversation_id_node_index` " +
                    "ON `message_node` (`conversation_id`, `node_index`)"
            )
            db.execSQL("DROP INDEX IF EXISTS `index_message_node_conversation_id`")
            db.execSQL("ALTER TABLE `workspaces` ADD COLUMN `storage_mounts` TEXT NOT NULL DEFAULT '[]'")
            Log.i(TAG, "migrate: migrate from 33 to 34 success")
        } finally {
            DatabaseMigrationTracker.onMigrationEnd()
        }
    }
}
