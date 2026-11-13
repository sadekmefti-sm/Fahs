package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FahsPackageIdentityTest {
    @Test
    fun verifyOfficialApplicationId() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("dz.fahs.app", BuildConfig.APPLICATION_ID)
        assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
    }
}
