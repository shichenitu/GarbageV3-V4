package dk.chen.garbagev1.ui.features.garbage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.chen.garbagev1.domain.Bin
import dk.chen.garbagev1.domain.BinRepository
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.domain.ItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import kotlin.String

@HiltViewModel
class GarbageListViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val binRepository: BinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sortingListArgs: SortingList = savedStateHandle.toRoute()
    private val itemId: String? = sortingListArgs.itemId

    private val _uiState = MutableStateFlow(value = UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = itemRepository.getGarbageList(),
                flow2 = binRepository.getBins()
            ) { garbageList, bins ->
                _uiState.update { it.copy(garbageList = garbageList, bins = bins) }
            }.collect {}
        }
        itemId?.let {
            viewModelScope.launch {
                itemRepository.getItem(id = itemId).collect { item ->
                    _uiState.update { it.copy(selectedItem = item) }
                }
            }
        }
    }

    val uiEvents: UiEvents = object : UiEvents {
        override fun onAddItemClick() {
            viewModelScope.launch {
                _navigationEvents.emit(value = NavigationEvent.NavigateToAddWhat)
            }
        }

        override fun onEditItemClick(item: Item) {
            _uiState.update { it.copy(selectedItem = item) }
        }

        override fun onWhatChange(what: String) {
            _uiState.update { it.copy(selectedItem = it.selectedItem?.copy(what = what)) }
        }

        override fun onWhereChange(where: String) {
            _uiState.update { it.copy(selectedItem = it.selectedItem?.copy(where = where)) }
        }

        override fun onPhotoCaptured(path: String) {
            _uiState.update { it.copy(selectedItem = it.selectedItem?.copy(photoPath = path)) }
        }

        override fun onUpClick() {
            viewModelScope.launch {
                _navigationEvents.emit(value = NavigationEvent.NavigateUp)
            }
        }

        override fun onSaveClick(): Boolean {
            val item = _uiState.value.selectedItem ?: return true

            when {
                item.what.isNotBlank() && item.where.isNotBlank() -> {
                    _uiState.update { it.copy(isWhatError = false, isWhereError = false) }
                    viewModelScope.launch {
                        itemRepository.updateItem(item = item)
                    }
                    onDismissDetails()
                    return true
                }

                item.what.isBlank() && item.where.isBlank() ->
                    _uiState.update { it.copy(isWhatError = true, isWhereError = true) }

                item.what.isBlank() -> _uiState.update {
                    it.copy(
                        isWhatError = true,
                        isWhereError = false
                    )
                }

                item.where.isBlank() -> _uiState.update {
                    it.copy(
                        isWhatError = false,
                        isWhereError = true
                    )
                }
            }
            return false
        }

        override fun onDeleteClick() {
            // TODO Show the delete confirmation dialog
            _uiState.update { it.copy(showDeleteConfirmation = true) }
        }

        override fun onConfirmDelete() {
            _uiState.value.selectedItem?.let { item ->
                viewModelScope.launch {
                    itemRepository.removeItem(item)
                }
            }
            onDismissDetails()
        }

        override fun onDismissDeleteConfirmation() {
            // TODO Hide the delete confirmation dialog
            _uiState.update { it.copy(showDeleteConfirmation = false) }
        }

        override fun onDismissDetails() {
            _uiState.update { it.copy(selectedItem = null, showDeleteConfirmation = false) }
        }
    }

    data class UiState(
        val garbageList: List<Item> = emptyList(),
        val bins: List<Bin> = emptyList(),
        val selectedItem: Item? = null,
        val isWhatError: Boolean = false,
        val isWhereError: Boolean = false,
        val showDeleteConfirmation: Boolean = false
    )

    @Immutable
    interface UiEvents {
        fun onAddItemClick()
        fun onEditItemClick(item: Item)
        fun onWhatChange(what: String)
        fun onWhereChange(where: String)
        fun onPhotoCaptured(path: String)
        fun onSaveClick(): Boolean
        fun onUpClick()
        fun onDeleteClick()
        fun onConfirmDelete()
        fun onDismissDeleteConfirmation()
        fun onDismissDetails()
    }

    sealed class NavigationEvent {
        data object NavigateToAddWhat : NavigationEvent()
        data class NavigateToDetails(val itemId: String) : NavigationEvent()
        data object NavigateUp : NavigationEvent()
    }
}