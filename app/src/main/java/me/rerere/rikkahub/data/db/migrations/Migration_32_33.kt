package me.rerere.rikkahub.data.db.migrations

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec

@DeleteTable(tableName = "translation_history")
class Migration_32_33 : AutoMigrationSpec
