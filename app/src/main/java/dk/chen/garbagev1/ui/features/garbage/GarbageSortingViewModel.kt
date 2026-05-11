package dk.chen.garbagev1.ui.features.garbage

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.chen.garbagev1.R
import dk.chen.garbagev1.domain.Bin
import dk.chen.garbagev1.domain.BinRepository
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.domain.ItemRepository
import dk.chen.garbagev1.ui.components.SnackBarHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class GarbageSortingViewModel @Inject constructor (
    private val itemRepository: ItemRepository,
    private val snackBarHandler: SnackBarHandler,
    private val binRepository: BinRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    sealed class NavigationEvent {
        data object NavigateToList : NavigationEvent()
        data object NavigateToAdd : NavigationEvent()
        data object NavigateToAffaldKbh : NavigationEvent()
    }

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    val bins: StateFlow<List<Bin>> = binRepository.getBins()
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val sortingListVisibility: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
    private val sortingList: StateFlow<List<Item>> =
        itemRepository.getGarbageList()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList()
            )

    private val itemWhat: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val itemWhere: MutableStateFlow<String> = MutableStateFlow(value = "")

    val uiState: StateFlow<UiState> = combine(
        flow = sortingList,
        flow2 = sortingListVisibility,
        flow3 = itemWhat,
        flow4 = itemWhere
    ) { currentShoppingList, currentListVisibility, what, where ->
        UiState(
            sortingList = currentShoppingList,
            displaySortingList = currentListVisibility,
            itemWhat = what,
            itemWhere = where,
            toggleListVisibilityButtonLabel = if (currentListVisibility) R.string.search_item_label else R.string.show_sorting_list_label
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(stopTimeoutMillis = 500),
        initialValue = UiState()
    )

    val uiEvents = object : UiEvents {
        override fun onWhatChange(newValue: String) {
            itemWhat.update { newValue }
        }

        override fun onWhereChange(newValue: String) {
            itemWhere.update { newValue }
        }

        override fun onSearchClick(itemWhat: String) {
            if (itemWhat.isNotBlank()) {
                viewModelScope.launch {
                    val foundItem = sortingList.value.find { it.what.equals(itemWhat, ignoreCase = true) }
                    if (foundItem != null) {
                        itemWhere.update { "${itemWhat} should be placed in: ${foundItem.where}" }
                    } else {
                        itemWhere.update { "${itemWhat} not found" }
                    }
                }
            } else {
                snackBarHandler.postMessage(msg = context.getString(R.string.textfield_error_message))
            }
        }

        override fun onRemoveItemClick(item: Item) {
            viewModelScope.launch {
                itemRepository.removeItem(item)
                snackBarHandler.postMessage(
                    msg = context.getString(R.string.item_removed_label, item.what, item.where),
                    actionLabel = context.getString(R.string.undo_label),
                    onActionClick = {
                        viewModelScope.launch {
                            itemRepository.addItem(item)
                            snackBarHandler.postMessage(msg = context.getString(R.string.undo_confirmation_message))
                        }
                    }
                )
            }
        }

        override fun onToggleListVisibilityClick() {
            viewModelScope.launch {
                _navigationEvents.emit(NavigationEvent.NavigateToList)
            }
        }

        override fun onAddItemClick() {
            viewModelScope.launch {
                _navigationEvents.emit(NavigationEvent.NavigateToAdd)
            }
        }

        override fun onAffaldKbhClick() {
            viewModelScope.launch {
                _navigationEvents.emit(NavigationEvent.NavigateToAffaldKbh)
            }
        }

        override fun onTrackRecyclingClick(bin: Bin) {
            viewModelScope.launch {
                val currentTime = System.currentTimeMillis()
                binRepository.updateBinPickupTime(bin.name, currentTime)
                snackBarHandler.postMessage(msg = "${bin.name} recycling tracked!")
            }
        }
    }

    data class UiState(
        val sortingList: List<Item> = emptyList(),
        val displaySortingList: Boolean = false,
        val itemWhat: String = "",
        val itemWhere: String = "",
        @get:StringRes val toggleListVisibilityButtonLabel: Int = R.string.show_sorting_list_label,
        val isError: Boolean = false
    )

    @Immutable
    interface UiEvents {
        fun onSearchClick(itemWhat: String)
        fun onRemoveItemClick(item: Item)
        fun onToggleListVisibilityClick()
        fun onWhatChange(newValue: String)
        fun onWhereChange(newValue: String)
        fun onAddItemClick()
        fun onAffaldKbhClick()
        fun onTrackRecyclingClick(bin: Bin)
    }
}