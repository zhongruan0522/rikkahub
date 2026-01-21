package ruan.rikkahub.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import me.rerere.ai.core.TokenUsage
import ruan.rikkahub.data.db.dao.ConversationDAO
import ruan.rikkahub.data.db.dao.GenMediaDAO
import ruan.rikkahub.data.db.dao.MemoryDAO
import ruan.rikkahub.data.db.dao.MessageNodeDAO
import ruan.rikkahub.data.db.entity.ConversationEntity
import ruan.rikkahub.data.db.entity.GenMediaEntity
import ruan.rikkahub.data.db.entity.MemoryEntity
import ruan.rikkahub.data.db.entity.MessageNodeEntity
import ruan.rikkahub.data.db.migrations.Migration_8_9
import ruan.rikkahub.utils.JsonInstant

@Database(
    entities = [ConversationEntity::class, MemoryEntity::class, GenMediaEntity::class, MessageNodeEntity::class],
    version = 12,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9, spec = Migration_8_9::class),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
    ]
)
@TypeConverters(TokenUsageConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDAO

    abstract fun memoryDao(): MemoryDAO

    abstract fun genMediaDao(): GenMediaDAO

    abstract fun messageNodeDao(): MessageNodeDAO
}

object TokenUsageConverter {
    @TypeConverter
    fun fromTokenUsage(usage: TokenUsage?): String {
        return JsonInstant.encodeToString(usage)
    }

    @TypeConverter
    fun toTokenUsage(usage: String): TokenUsage? {
        return JsonInstant.decodeFromString(usage)
    }
}

