package dk.chen.garbagev1.ui.features.garbage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.chen.garbagev1.domain.Item
import dk.chen.garbagev1.domain.ItemRepository
import dk.chen.garbagev1.ui.components.VALID_INPUT_REGEX
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class AddWhereViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val addWhereArgs: AddWhere = savedStateHandle.toRoute()
    private val what: String = addWhereArgs.what

    private val _uiState = MutableStateFlow(value = UiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    val uiEvents: UiEvents = object : UiEvents {
        override fun onWhereChange(where: String) {
            if (where.matches(VALID_INPUT_REGEX)) {
                _uiState.update { it.copy(where = where, isError = false) }
            }
        }

        override fun onDoneClick() {
            if (what.isNotBlank() && uiState.value.where.isNotBlank()) {
                viewModelScope.launch {
                    itemRepository.addItem(item = Item(what = what, where = uiState.value.where))
                            _navigationEvents.emit(value = NavigationEvent.NavigateToGarbageList)
                }
            } else {
                _uiState.update { it.copy(isError = true) }
            }
        }

        override fun onUpClick() {
            viewModelScope.launch {
                _navigationEvents.emit(value = NavigationEvent.CloseDialog)
            }
        }

        override fun onCancelClick() {
            // TODO("Not yet implemented")
            viewModelScope.launch {
                _navigationEvents.emit(value = NavigationEvent.NavigateToGarbageList)
            }
        }
    }

    data class UiState(
        val where: String = "",
        val isError: Boolean = false
    )

    @Immutable
    interface UiEvents {
        fun onWhereChange(where: String)
        fun onDoneClick()
        fun onUpClick()
        fun onCancelClick()
    }

    sealed class NavigationEvent {
        data object CloseDialog : NavigationEvent()
        data object NavigateToGarbageList : NavigationEvent()
    }
}