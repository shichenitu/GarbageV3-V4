package dk.chen.garbagev1.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dk.chen.garbagev1.R

val VALID_INPUT_REGEX = Regex("^[\\p{L} ]*$")

@Composable
fun GarbageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelRes: Int,
    focusManager: FocusManager,
    isLastField: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    @StringRes supportingTextRes: Int? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = stringResource(id = labelRes)) },
        isError = isError,
        modifier = modifier.padding(vertical = 8.dp),
        minLines = minLines,
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLastField) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() }
        ),
        supportingText = {
            if (isError) {
                Text(text = stringResource(id = supportingTextRes ?: R.string.textfield_error_message))
            }
        }
    )
}