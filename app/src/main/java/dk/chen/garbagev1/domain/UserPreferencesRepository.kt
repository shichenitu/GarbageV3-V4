package dk.chen.garbagev1.domain

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val theme: Flow<Theme>
    suspend fun setTheme(theme: Theme)
    
    val language: Flow<String>
    suspend fun setLanguage(language: String)
}