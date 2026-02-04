package ruan.rikkahub.data.datastore.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import ruan.rikkahub.data.datastore.SettingsStore
import ruan.rikkahub.utils.JsonInstant
import ruan.rikkahub.utils.migrateLegacyPolymorphicTypes

class PreferenceStoreV2Migration : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val version = currentData[SettingsStore.VERSION]
        return version == null || version < 2
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val prefs = currentData.toMutablePreferences()

        prefs[SettingsStore.DISPLAY_SETTING]?.let { raw ->
            prefs[SettingsStore.DISPLAY_SETTING] = runCatching {
                val migrated = JsonInstant.parseToJsonElement(raw).migrateLegacyPolymorphicTypes()
                JsonInstant.encodeToString(migrated)
            }.getOrElse { "{}" }
        }

        prefs[SettingsStore.ASSISTANTS]?.let { raw ->
            prefs[SettingsStore.ASSISTANTS] = runCatching {
                val migrated = JsonInstant.parseToJsonElement(raw).migrateLegacyPolymorphicTypes()
                JsonInstant.encodeToString(migrated)
            }.getOrElse { "[]" }
        }

        prefs[SettingsStore.VERSION] = 2

        return prefs.toPreferences()
    }

    override suspend fun cleanUp() {}
}

