package dk.chen.garbagev1.ui.features.garbage

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.ui.components.BooleanProvider
import dk.chen.garbagev1.ui.components.GarbageTextField
import dk.chen.garbagev1.ui.components.ThemedPreviews
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsSheet(
    item: Item,
    isWhatError: Boolean,
    isWhereError: Boolean,
    showDeleteConfirmation: Boolean,
    uiEvents: GarbageListViewModel.UiEvents,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val outerPaneTitle = stringResource(id = R.string.edit_item_title)

    if (showDeleteConfirmation) {
        // TODO Add delete confirmation dialog. Hint: use AlertDialog
        AlertDialog(
            onDismissRequest = uiEvents::onDismissDeleteConfirmation,
            title = {
                Text(text = stringResource(id = R.string.delete_item_title))
            },
            text = {
                Text(text = stringResource(id = R.string.delete_item_confirmation))
            },
            confirmButton = {
                TextButton(onClick = uiEvents::onConfirmDelete) {
                    Text(text = stringResource(id = R.string.delete_button_label))
                }
            },
            dismissButton = {
                TextButton(onClick = uiEvents::onDismissDeleteConfirmation) {
                    Text(text = stringResource(id = R.string.cancel_button_label))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = uiEvents::onDismissDetails,
        sheetState = sheetState,
        dragHandle = null,
        modifier = modifier.semantics {
            paneTitle = outerPaneTitle
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            uiEvents.onDismissDetails()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close_button_label)
                    )
                }

                Text(
                    text = stringResource(id = R.string.edit_item_title),
                    modifier = Modifier.semantics { heading() }
                )

                TextButton(onClick = {
                    val canDismissSheet = uiEvents.onSaveClick()
                    if (canDismissSheet)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                uiEvents.onDismissDetails()
                            }
                        }
                }) {
                    Text(text = stringResource(id = R.string.save_button_label))
                }
            }

            Spacer(Modifier.height(height = 8.dp))

            ItemPhotoSection(
                item = item,
                onPhotoCaptured = uiEvents::onPhotoCaptured,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val focusManager = LocalFocusManager.current

            GarbageTextField(
                value = item.what,
                onValueChange = uiEvents::onWhatChange,
                labelRes = R.string.what_label,
                focusManager = focusManager,
                isLastField = false,
                isError = isWhatError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
            )

            GarbageTextField(
                value = item.where,
                onValueChange = uiEvents::onWhereChange,
                labelRes = R.string.where_label,
                focusManager = focusManager,
                isLastField = false,
                isError = isWhereError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
            )

            Spacer(Modifier.height(height = 8.dp))

            Button(onClick = uiEvents::onDeleteClick) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) { }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )

                    Spacer(Modifier.height(height = 8.dp))

                    Text(text = stringResource(id = R.string.delete_button_label))
                }
            }
        }
    }
}

@Composable
fun ItemPhotoSection(
    item: Item,
    onPhotoCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempFilePath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempFilePath != null) {
            onPhotoCaptured(tempFilePath!!)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.filesDir, "item_${item.id}.jpg")
            if (!file.exists()) {
                file.createNewFile()
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            tempFilePath = file.absolutePath
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (item.photoPath != null) {
            AsyncImage(
                model = item.photoPath,
                contentDescription = stringResource(R.string.garbage_memo),
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Change Photo")
            }
        } else {
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.snap_a_memo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text(stringResource(R.string.take_a_photo))
                    }
                }
            }
        }
    }
}

@ThemedPreviews
@Composable
fun DetailsSheetPreview(@PreviewParameter(provider = BooleanProvider::class) isTrue: Boolean) {
    GarbageV1Theme {
        DetailsSheet(
            item = Item(what = "Book", where = "Paper"),
            isWhatError = isTrue,
            isWhereError = isTrue,
            showDeleteConfirmation = isTrue,
            uiEvents = object : GarbageListViewModel.UiEvents {
                override fun onWhatChange(what: String) {}
                override fun onWhereChange(where: String) {}
                override fun onPhotoCaptured(path: String) {}
                override fun onSaveClick(): Boolean {
                    return true
                }
                override fun onUpClick() {}
                override fun onDeleteClick() {}
                override fun onConfirmDelete() {}
                override fun onDismissDeleteConfirmation() {}
                override fun onDismissDetails() {}
                override fun onAddItemClick() {}
                override fun onEditItemClick(item: Item) {}
            },
        )
    }
}