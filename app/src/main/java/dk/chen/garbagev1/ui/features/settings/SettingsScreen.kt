package dk.chen.garbagev1.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Theme
import dk.chen.garbagev1.ui.components.ThemedPreviews
import dk.chen.garbagev1.ui.navigation.AppRoute
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme
import kotlinx.serialization.Serializable
import android.app.Activity
import androidx.activity.compose.LocalActivity

@Serializable
object Settings : AppRoute

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(uiState, viewModel.uiEvents)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: SettingsViewModel.UiState,
    uiEvents: SettingsViewModel.UiEvents
) {
    var expanded by remember { mutableStateOf(value = false) }
    var languageExpanded by remember { mutableStateOf(false) }
    val activity = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.theme_label),
                color = MaterialTheme.colorScheme.onBackground
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val currentThemeText = when (uiState.theme) {
                    Theme.LIGHT -> stringResource(id = R.string.theme_light)
                    Theme.DARK -> stringResource(id = R.string.theme_dark)
                    Theme.SYSTEM -> stringResource(id = R.string.theme_system)
                }

                TextField(
                    value = currentThemeText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                )
                // TODO: Add an ExposedDropdownMenu & a DropdownMenuItem for each Theme.entries. NB: Theme names needs to be lowercase.
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Theme.entries.forEach { theme ->
                        val itemThemeText = when (theme) {
                            Theme.LIGHT -> stringResource(id = R.string.theme_light)
                            Theme.DARK -> stringResource(id = R.string.theme_dark)
                            Theme.SYSTEM -> stringResource(id = R.string.theme_system)
                        }

                        DropdownMenuItem(
                            text = {
                                Text(text = itemThemeText)
                            },
                            onClick = {
                                uiEvents.onSetTheme(theme)
                                expanded = false
                                activity?.recreate()
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.language_label),
                color = MaterialTheme.colorScheme.onBackground
            )
            ExposedDropdownMenuBox(
                expanded = languageExpanded,
                onExpandedChange = { languageExpanded = !languageExpanded }
            ) {
                val currentLanguageText = when (uiState.currentLanguage) {
                    SettingsViewModel.AppLanguage.ENGLISH -> "English"
                    SettingsViewModel.AppLanguage.DANISH -> "Dansk"
                }

                TextField(
                    value = currentLanguageText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                    modifier = Modifier.menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    )
                )

                ExposedDropdownMenu(
                    expanded = languageExpanded,
                    onDismissRequest = { languageExpanded = false }
                ) {
                    SettingsViewModel.AppLanguage.entries.forEach { language ->
                        val itemLanguageText = when (language) {
                            SettingsViewModel.AppLanguage.ENGLISH -> "English"
                            SettingsViewModel.AppLanguage.DANISH -> "Dansk"
                        }

                        DropdownMenuItem(
                            text = { Text(text = itemLanguageText) },
                            onClick = {
                                uiEvents.onSetLanguage(language)
                                languageExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@ThemedPreviews
@Composable
private fun SettingsScreenPreview() {
    GarbageV1Theme {
        SettingsScreen(
            uiState = SettingsViewModel.UiState(),
            uiEvents = object : SettingsViewModel.UiEvents {
                override fun onSetTheme(theme: Theme) {}
                override fun onSetLanguage(language: SettingsViewModel.AppLanguage) {}
            }
        )
    }
}
