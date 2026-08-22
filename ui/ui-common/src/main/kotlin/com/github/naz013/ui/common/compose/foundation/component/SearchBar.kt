package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton

/**
 * A rounded, single-line search input following Material 3 styling.
 *
 * Displays a search icon on the leading edge and, once [query] is non-empty, a clear icon
 * on the trailing edge that resets the query back to an empty string.
 *
 * @param query Current text of the search field
 * @param onQueryChange Callback invoked with the new text whenever it changes
 * @param modifier Optional modifier for the search field
 * @param placeholder Hint text shown when [query] is empty
 */
@Composable
fun SearchBar(
  modifier: Modifier = Modifier,
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String = ""
) {
  val shape = remember { RoundedCornerShape(28.dp) }

  TextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier,
    placeholder = if (placeholder.isNotEmpty()) {
      { Text(text = placeholder) }
    } else {
      null
    },
    leadingIcon = {
      Icon(
        painter = AppIcons.Fluent.Search,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    trailingIcon = {
      AnimatedVisibility(
        visible = query.isNotEmpty(),
        enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
          fadeIn(),
        exit = scaleOut(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
          fadeOut()
      ) {
        TooltipIconButton(contentDescription = stringResource(R.string.cd_clear_search)) {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(
              painter = AppIcons.Fluent.Dismiss,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxSize()
                .padding(10.dp)
            )
          }
        }
      }
    },
    singleLine = true,
    shape = shape,
    colors = TextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      focusedIndicatorColor = Color.Transparent,
      unfocusedIndicatorColor = Color.Transparent,
      disabledIndicatorColor = Color.Transparent,
      cursorColor = MaterialTheme.colorScheme.primary
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
  )
}

@Preview(showBackground = true, name = "Search bar - empty")
@Composable
private fun PreviewSearchBarEmpty() {
  AppTheme {
    SearchBar(
      query = "",
      onQueryChange = {},
      placeholder = "Search"
    )
  }
}

@Preview(showBackground = true, name = "Search bar - with query")
@Composable
private fun PreviewSearchBarWithQuery() {
  AppTheme {
    SearchBar(
      query = "Groceries",
      onQueryChange = {},
      placeholder = "Search"
    )
  }
}
