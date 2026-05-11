package dk.chen.garbagev1.ui.features.garbage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Bin
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme
import dk.chen.garbagev1.domain.fullDescription
import dk.chen.garbagev1.ui.components.GarbageTopAppBar
import dk.chen.garbagev1.ui.components.GarbageTextField
import dk.chen.garbagev1.ui.components.NavigationType

@Composable
fun GarbageSortingScreen(
    onNavigate: (GarbageSortingViewModel.NavigationEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GarbageSortingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvents.collect { event ->
            onNavigate(event)
        }
    }

    GarbageSortingScreen(
        modifier = modifier,
        uiState = uiState,
        uiEvents = viewModel.uiEvents
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GarbageSortingScreen(
    modifier: Modifier = Modifier,
    uiState: GarbageSortingViewModel.UiState,
    uiEvents: GarbageSortingViewModel.UiEvents
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            GarbageTopAppBar(
                titleRes = R.string.app_name,
                navigationType = NavigationType.NONE
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                GarbageTextField(
                    value = uiState.itemWhat,
                    onValueChange = uiEvents::onWhatChange,
                    labelRes = R.string.what_label,
                    focusManager = LocalFocusManager.current,
                    isLastField = false,
                    isError = uiState.isError
                )
            }

            item {
                if (uiState.itemWhere.isNotEmpty()) {
                    Card(
                        modifier = Modifier.padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = uiState.itemWhere,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            if (uiState.displaySortingList) {
                item {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                uiState.sortingList.forEach { item ->
                    item {
                        Text(
                            text = item.fullDescription(),
                            modifier = Modifier.clickable { uiEvents.onRemoveItemClick(item) }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        uiEvents.onSearchClick(itemWhat = uiState.itemWhat)
                        focusManager.clearFocus()
                    }) {
                        Text(text = stringResource(id = R.string.where_label))
                    }
                    Button(onClick = uiEvents::onToggleListVisibilityClick) {
                        Text(text = stringResource(R.string.show_sorting_list))
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = uiEvents::onAffaldKbhClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                ) {
                    Text(text = stringResource(R.string.visit_affaldkbh_website))
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GarbageSortingScreenPreview() {
    GarbageV1Theme {
        GarbageSortingScreen(
            modifier = Modifier,
            uiState = GarbageSortingViewModel.UiState(),
            uiEvents = object : GarbageSortingViewModel.UiEvents {
                override fun onSearchClick(itemWhat: String) {}
                override fun onRemoveItemClick(item: Item) {}
                override fun onToggleListVisibilityClick() {}
                override fun onWhatChange(newValue: String) {}
                override fun onWhereChange(newValue: String) {}
                override fun onAddItemClick() {}
                override fun onAffaldKbhClick() {}
                override fun onTrackRecyclingClick(bin: Bin) {}
            }
        )
    }
}