package ruan.rikkahub.data.datastore

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReadinessTest {

    @Test
    fun `dummy settings should be marked as init`() {
        assertTrue(Settings.dummy().init)
    }

    @Test
    fun `first non-init settings should wait for readiness`() = runBlocking {
        val settingsFlow = MutableStateFlow(Settings.dummy())
        val ready = Settings(init = false)

        val deferred = async {
            settingsFlow.first { !it.init }
        }

        yield()
        assertFalse(deferred.isCompleted)

        settingsFlow.value = ready
        assertSame(ready, deferred.await())
    }
}

