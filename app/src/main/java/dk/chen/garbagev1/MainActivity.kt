package dk.chen.garbagev1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import dk.chen.garbagev1.core.workers.DeadlineNotificationWorker
import dk.chen.garbagev1.domain.Theme
import dk.chen.garbagev1.ui.components.RequestNotificationPermission
import dk.chen.garbagev1.ui.features.settings.SettingsViewModel
import dk.chen.garbagev1.ui.navigation.MainNavigation
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme
import java.util.concurrent.TimeUnit
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import android.os.LocaleList
import android.content.res.Configuration
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val locales = AppCompatDelegate.getApplicationLocales()
        Log.d("MainActivityLocale", "attachBaseContext locales: ${locales.toLanguageTags()}")
        if (!locales.isEmpty) {
            val config = Configuration(newBase.resources.configuration)
            config.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
            super.attachBaseContext(newBase.createConfigurationContext(config))
            applyOverrideConfiguration(config)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val config = Configuration(resources.configuration)
            config.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
            applyOverrideConfiguration(config)
        }
        super.onCreate(savedInstanceState)
        Log.d("MainActivityLocale", "onCreate locales: ${AppCompatDelegate.getApplicationLocales().toLanguageTags()}")
        Log.d("MainActivityLocale", "onCreate config locales: ${resources.configuration.locales.toLanguageTags()}")


        val workRequest = PeriodicWorkRequestBuilder<DeadlineNotificationWorker>(
            8, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RecyclingCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        enableEdgeToEdge()

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (uiState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }

            val currentLanguage = uiState.currentLanguage
            val context = androidx.compose.ui.platform.LocalContext.current
            val currentConfig = androidx.compose.ui.platform.LocalConfiguration.current
            val composeConfig = android.content.res.Configuration(currentConfig)
            composeConfig.setLocales(android.os.LocaleList.forLanguageTags(currentLanguage.tag))
            
            val localizedContext = object : android.content.ContextWrapper(context) {
                override fun getResources(): android.content.res.Resources {
                    return context.createConfigurationContext(composeConfig).resources
                }
            }

            // We call enableEdgeToEdge here, inside setContent, to recompose when the theme changes.
            // This ensures the status and navigation bar colors update dynamically.
            // The SystemBarStyle.auto() constructor requires us to explicitly provide the scrim
            // colors. We are using the library's default values to maintain standard behavior.
            enableEdgeToEdge(
                statusBarStyle = androidx.activity.SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT,
                ) { darkTheme },
                navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.argb(
                        /* alpha = */0xe6,
                        /* red = */ 0xFF,
                        /* green = */ 0xFF,
                        /* blue = */ 0xFF
                    ),
                    darkScrim = android.graphics.Color.argb(
                        /* alpha = */ 0x80,
                        /* red = */ 0x1b,
                        /* green = */ 0x1b,
                        /* blue = */ 0x1b
                    ),
                ) { darkTheme },
            )

            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides composeConfig
            ) {
                GarbageV1Theme() {
                    RequestNotificationPermission()
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainNavigation()
                    }
                }
            }
        }
    }
}