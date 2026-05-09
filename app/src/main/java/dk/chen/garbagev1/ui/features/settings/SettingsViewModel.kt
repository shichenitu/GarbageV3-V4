package dk.chen.garbagev1.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.chen.garbagev1.domain.Theme
import dk.chen.garbagev1.domain.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private fun getCurrentAppLanguage(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (locales.isEmpty) {
            "en"
        } else {
            locales.get(0)?.language ?: "en"
        }

        return AppLanguage.entries.find { it.tag == currentTag } ?: AppLanguage.ENGLISH
    }

    val uiState: StateFlow<UiState> = combine(
        userPreferencesRepository.theme,
        userPreferencesRepository.language
    ) { theme, languageTag ->
        val appLanguage = AppLanguage.entries.find { it.tag == languageTag } ?: AppLanguage.ENGLISH
        UiState(
            theme = theme,
            currentLanguage = appLanguage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = UiState()
    )

    val uiEvents: UiEvents = object : UiEvents {
        override fun onSetTheme(theme: Theme) {
            viewModelScope.launch {
                userPreferencesRepository.setTheme(theme)
            }
        }

        override fun onSetLanguage(language: AppLanguage) {
            val localeList = LocaleListCompat.forLanguageTags(language.tag)
            AppCompatDelegate.setApplicationLocales(localeList)
            viewModelScope.launch {
                userPreferencesRepository.setLanguage(language.tag)
            }
        }
    }

    enum class AppLanguage(val tag: String) {
        ENGLISH("en"),
        DANISH("da")
    }

    data class UiState(
        val theme: Theme = Theme.SYSTEM,
        val currentLanguage: AppLanguage = AppLanguage.ENGLISH
    )

    @Immutable
    interface UiEvents {
        fun onSetTheme(theme: Theme)
        fun onSetLanguage(language: AppLanguage)
    }
}
