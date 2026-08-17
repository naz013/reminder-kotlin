package com.github.naz013.ui.map

import android.location.Address
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import com.github.naz013.ui.common.R

@Composable
internal fun AddressSearchField(
  query: String,
  suggestions: List<Address>,
  onQueryChange: (String) -> Unit,
  onSuggestionSelected: (Address) -> Unit,
  onDismissSuggestions: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current

  Box(modifier = modifier) {
    TextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(stringResource(R.string.builder_search_address)) },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
    DropdownMenu(
      expanded = suggestions.isNotEmpty(),
      onDismissRequest = onDismissSuggestions,
      // DropdownMenu opens as a focusable Popup by default, which steals focus from the
      // TextField above and dismisses the IME the moment suggestions appear - keep typing
      // possible while the list is showing.
      properties = PopupProperties(focusable = false),
    ) {
      suggestions.forEach { address ->
        DropdownMenuItem(
          text = { AddressSuggestion(address) },
          onClick = {
            focusManager.clearFocus()
            onSuggestionSelected(address)
          },
        )
      }
    }
  }
}

@Composable
private fun AddressSuggestion(address: Address) {
  val primary = address.toDisplayTitle()
  val secondary = if (address.getAddressLine(0) != null) address.toShortDisplayName() else ""
  Column {
    Text(text = primary, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    if (secondary.isNotEmpty()) {
      Text(text = secondary, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}
