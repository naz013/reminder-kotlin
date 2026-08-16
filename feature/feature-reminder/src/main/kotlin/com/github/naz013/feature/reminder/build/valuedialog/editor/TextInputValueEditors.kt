package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.github.naz013.feature.reminder.build.BuilderItem

/** Plain single-line text field, styled per [keyboardType]. Replaces `EmailInputController` and
 *  `WebAddressInputController`, which only differ from a generic text field by keyboard type and
 *  validation (validation stays in [com.elementary.tasks.reminder.build.bi.BuilderModifier] -
 *  unchanged by this port). */
@Composable
fun SimpleTextValueEditor(
  builderItem: BuilderItem<String>,
  onValueChange: (BuilderItem<*>) -> Unit,
  keyboardType: KeyboardType,
) {
  var text by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue() ?: "") }
  OutlinedTextField(
    value = text,
    onValueChange = {
      text = it
      builderItem.modifier.update(it)
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
  )
}
