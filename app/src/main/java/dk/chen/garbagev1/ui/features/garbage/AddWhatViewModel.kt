package dk.chen.garbagev1.ui.features.garbage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.chen.garbagev1.ui.components.VALID_INPUT_REGEX
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable

class AddWhatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(value = UiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    val uiEvents: UiEvents = object : UiEvents {
        override fun onWhatChange(what: String) {
            if (what.matches(VALID_INPUT_REGEX)) {
                _uiState.update { it.copy(what = what, isError = false) }
            }
        }

        override fun onNextClick() {
            if (_uiState.value.what.isNotBlank()) {
                viewModelScope.launch {
                    _navigationEvents.emit(value = NavigationEvent.NavigateToAddWhere(uiState.value.what))
                }
            } else {
                _uiState.update { it.copy(isError = true) }
            }
        }

        override fun onUpClick() {
            viewModelScope.launch {
                _navigationEvents.emit(value = NavigationEvent.NavigateUp)
            }
        }
    }

    data class UiState(
        val what: String = "",
        val isError: Boolean = false
    )

    @Immutable
    interface UiEvents {
        fun onWhatChange(what: String)
        fun onNextClick()
        fun onUpClick()
    }

    sealed class NavigationEvent {
        data object NavigateUp : NavigationEvent()
        data class NavigateToAddWhere(val what: String) : NavigationEvent()
    }
}