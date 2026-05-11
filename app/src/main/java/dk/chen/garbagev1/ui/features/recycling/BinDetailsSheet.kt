package dk.chen.garbagev1.ui.features.recycling

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dk.chen.garbagev1.domain.Bin
import dk.chen.garbagev1.ui.components.BinProvider
import dk.chen.garbagev1.ui.components.ThemedPreviews
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.ui.features.garbage.GarbageSortingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinDetailsSheet(
    bin: Bin,
    onTrackClick: (Bin) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = bin.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(height = 16.dp))
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(height = 24.dp))

            Button(
                onClick = {
                    onTrackClick(bin)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = bin.binColor)
            ) {
                Text(text = stringResource(R.string.track_recycling), color = Color.White)
            }

            Spacer(modifier = Modifier.height(height = 16.dp))
        }
    }
}

@ThemedPreviews
@Composable
private fun ShopDetailsSheetPreview(@PreviewParameter(provider = BinProvider::class) bin: Bin) {
    val mockEvents = object : GarbageSortingViewModel.UiEvents {
        override fun onSearchClick(itemWhat: String) {}
        override fun onRemoveItemClick(item: Item) {}
        override fun onToggleListVisibilityClick() {}
        override fun onWhatChange(newValue: String) {}
        override fun onWhereChange(newValue: String) {}
        override fun onAddItemClick() {}
        override fun onAffaldKbhClick() {}
        override fun onTrackRecyclingClick(bin: Bin) {}
    }

    GarbageV1Theme {
        BinDetailsSheet(
            bin = bin,
            onTrackClick = {},
            onDismiss = {}
        )
    }
}