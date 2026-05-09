package dk.chen.garbagev1

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import android.util.Log

@RunWith(AndroidJUnit4::class)
class LocaleTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLocaleChange() {
        activityRule.scenario.onActivity { activity ->
            // Change to Danish
            val localeList = LocaleListCompat.forLanguageTags("da")
            AppCompatDelegate.setApplicationLocales(localeList)
            activity.recreate()
        }

        // Wait a bit for recreate
        Thread.sleep(2000)

        activityRule.scenario.onActivity { activity ->
            val settingsTitle = activity.getString(R.string.settings_screen_title)
            Log.d("LocaleTest", "String is: $settingsTitle")
            // In Danish it is "Indstillinger", in English it is "Settings"
            assertEquals("Indstillinger", settingsTitle)
        }
    }
}
