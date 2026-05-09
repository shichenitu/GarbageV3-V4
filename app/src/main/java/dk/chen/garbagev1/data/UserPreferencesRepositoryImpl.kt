package dk.chen.garbagev1.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.chen.garbagev1.domain.Theme
import dk.chen.garbagev1.domain.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    UserPreferencesRepository {

    private object PreferencesKeys {
        val THEME = stringPreferencesKey(name = "theme")
        val LANGUAGE = stringPreferencesKey(name = "language")
    }

    override val theme: Flow<Theme> = context.dataStore.data
        .map { preferences ->
            when (preferences[PreferencesKeys.THEME]) {
                Theme.LIGHT.name.lowercase() -> Theme.LIGHT
                Theme.DARK.name.lowercase() -> Theme.DARK
                else -> Theme.SYSTEM
            }
        }

    override val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "en"
        }

    override suspend fun setTheme(theme: Theme) {
        // TODO: Map preferences keys to values
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = when (theme) {
                Theme.LIGHT -> "light"
                Theme.DARK -> "dark"
                Theme.SYSTEM -> "system"
            }
        }
    }

    override suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }
}
