package dk.chen.garbagev1.ui.features.recycling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dk.chen.garbagev1.ui.navigation.AppRoute
import kotlinx.serialization.Serializable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dk.chen.garbagev1.ui.components.GarbageTopAppBar
import dk.chen.garbagev1.ui.components.NavigationType
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Bin
import dk.chen.garbagev1.domain.BinCategory
import dk.chen.garbagev1.domain.RecyclingStation
import dk.chen.garbagev1.domain.getDisplayNameRes
import dk.chen.garbagev1.ui.components.BinOrNullProvider
import dk.chen.garbagev1.ui.components.RequestBackgroundLocationPermission
import dk.chen.garbagev1.ui.components.ThemedPreviews
import dk.chen.garbagev1.ui.components.previewBins
import dk.chen.garbagev1.ui.theme.theme.GarbageV1Theme

@Serializable
object Bins : AppRoute

@Composable
fun formatTimeElapsed(lastPickupTime: Long): String? {
    if (lastPickupTime == 0L) return null

    val context = androidx.compose.ui.platform.LocalContext.current
    val diff = System.currentTimeMillis() - lastPickupTime
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> context.getString(R.string.time_elapsed_days, days.toInt(), (hours % 24).toInt())
        hours > 0 -> context.getString(R.string.time_elapsed_hours, hours.toInt(), (minutes % 60).toInt())
        else -> context.getString(R.string.just_now)
    }
}

@Composable
fun RecyclingScreen(
    modifier: Modifier = Modifier,
    viewModel: RecyclingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    BinsScreen(uiState = uiState, uiEvents = viewModel.uiEvents, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BinsScreen(
    uiState: RecyclingViewModel.UiState,
    uiEvents: RecyclingViewModel.UiEvents,
    modifier: Modifier = Modifier
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFilterCategory by remember { mutableStateOf<BinCategory?>(null) }

    if (uiState.showBackgroundPermission) {
        RequestBackgroundLocationPermission(
            onPermissionGranted = {
                uiEvents.onBackgroundPermissionHandled(true)
            },
            onPermissionDenied = {
                uiEvents.onBackgroundPermissionHandled(false)
            }
        )
    }

    Scaffold(
        topBar = {
            GarbageTopAppBar(
                titleRes = R.string.bins_near_you_title,
                navigationType = NavigationType.NONE
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ){
                    Button(
                        onClick = { uiEvents.onEnableGeofencingClick() },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = stringResource(id = R.string.enable_geofencing_button_label))
                    }
                }

                key(uiState.bins.size){
                    val carouselState = rememberCarouselState { uiState.bins.count() }

                    HorizontalUncontainedCarousel(
                        state = carouselState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        itemWidth = 186.dp,
                        itemSpacing = 8.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ){ i ->
                        val bin = uiState.bins[i]
                        Card(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(shape = MaterialTheme.shapes.extraLarge)
                                .clickable { uiEvents.onBinSelected(bin) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                AsyncImage(
                                    model = bin.imageUrl,
                                    contentDescription = stringResource(bin.getDisplayNameRes()),
                                    modifier = Modifier.weight(1f),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = stringResource(bin.getDisplayNameRes()),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(8.dp)
                                )

                                val timeElapsedStr = formatTimeElapsed(bin.lastPickupTime)
                                val isOverdue = bin.lastPickupTime != 0L &&
                                        (System.currentTimeMillis() - bin.lastPickupTime) > 7 * 24 * 3600 * 1000

                                val displayText = timeElapsedStr ?: stringResource(id = R.string.never_emptied)

                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = if (isOverdue) Color.Red else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = stringResource(R.string.recycling_stations_api),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = filterExpanded,
                        onExpandedChange = { filterExpanded = !filterExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = selectedFilterCategory?.let { stringResource(it.stringRes) } ?: stringResource(R.string.all_categories),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_categories)) },
                                onClick = {
                                    selectedFilterCategory = null
                                    filterExpanded = false
                                }
                            )
                            uiState.bins.forEach { bin ->
                                val category = BinCategory.fromName(bin.name)
                                DropdownMenuItem(
                                    text = { Text(stringResource(bin.getDisplayNameRes())) },
                                    onClick = {
                                        selectedFilterCategory = category
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val filteredStations = if (selectedFilterCategory == null) {
                uiState.stations
            } else {
                uiState.stations.filter { station ->
                    val cat = station.category.lowercase()

                    when (selectedFilterCategory) {
                        BinCategory.BATTERIES, BinCategory.PLASTIC, BinCategory.CARDBOARD,
                        BinCategory.GLASS, BinCategory.PAPER, BinCategory.METAL,
                        BinCategory.FOOD, BinCategory.DAILY_WASTE -> {
                            cat.contains("nærgenbrugsstation") || cat.contains("genbrugsstation")
                        }

                        BinCategory.ELECTRONICS, BinCategory.BULKY_WASTE, BinCategory.WOOD,
                        BinCategory.CHEMICAL, BinCategory.TEXTILE_WASTE, BinCategory.OTHER -> {
                            cat.contains("genbrugsstation") && !cat.contains("nær")
                        }

                        null -> true
                    }
                }
            }

            items(
                filteredStations
            ) { station ->
                StationCard(station = station)
            }
        }
    }

    uiState.selectedBin?.let {
        BinDetailsSheet(
            bin = it,
            onTrackClick = { bin -> uiEvents.onTrackRecyclingClick(bin) },
            onDismiss = uiEvents::onDismissBinDetails
        )
    }
}

@Composable
private fun StationCard(station: RecyclingStation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = station.name, style = MaterialTheme.typography.titleMedium)
            Text(text = station.address, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(
                    containerColor = if (station.status == "Aktiv")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                ) {
                    Text(text = station.status, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = station.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@ThemedPreviews
@Composable
private fun BinsScreenPreview(@PreviewParameter(provider = BinOrNullProvider::class) binOrNull: Bin?) {
    GarbageV1Theme {
        BinsScreen(
            uiState = RecyclingViewModel.UiState(
                bins = previewBins(),
                selectedBin = binOrNull
            ),
            uiEvents = object : RecyclingViewModel.UiEvents {
                override fun onBinSelected(bin: Bin) {}
                override fun onDismissBinDetails() {}
                override fun onTrackRecyclingClick(bin: Bin) {}
                override fun onEnableGeofencingClick() {}
                override fun onBackgroundPermissionHandled(isGranted: Boolean) {}
            }
        )
    }
}