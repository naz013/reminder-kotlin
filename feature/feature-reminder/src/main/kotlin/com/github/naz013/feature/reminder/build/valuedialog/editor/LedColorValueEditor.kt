package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.LedColorBuilderItem

private val LedSwatches = listOf(
  0 to R.color.led_red_button_tint,
  1 to R.color.led_green_button_tint,
  2 to R.color.led_blue_button_tint,
  3 to R.color.led_yellow_button_tint,
  4 to R.color.led_pink_button_tint,
  5 to R.color.led_orange_button_tint,
  6 to R.color.led_teal_button_tint,
)
private const val DEFAULT_LED_INDEX = 2

/** LED notification color swatch row. Replaces `LedColorController`'s `RadioGroup` of tinted
 *  `RadioButton`s with a row of tappable colored circles. */
@Composable
internal fun LedColorValueEditor(
  builderItem: LedColorBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var selected by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue() ?: DEFAULT_LED_INDEX)
  }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    LedSwatches.forEach { (index, colorRes) ->
      val isSelected = index == selected
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(colorResource(colorRes))
          .border(
            width = if (isSelected) 3.dp else 0.dp,
            color = MaterialTheme.colorScheme.onSurface,
            shape = CircleShape,
          ).clickable {
            selected = index
            builderItem.modifier.update(index)
            onValueChange(builderItem)
          },
      )
    }
  }
}
