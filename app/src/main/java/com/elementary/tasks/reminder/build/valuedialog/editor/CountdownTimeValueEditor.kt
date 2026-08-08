package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.reminder.build.BuilderItem
import com.github.naz013.datecalc.DateTimeManager

private const val EMPTY_TIME_STRING = "000000"
private val KEYPAD_ROWS = listOf(
  listOf(1, 2, 3),
  listOf(4, 5, 6),
  listOf(7, 8, 9),
)

/**
 * Countdown duration entry: a running HH:MM:SS display fed by a numeric keypad, where each digit
 * push shifts the 6-character window left (matching `TimerPickerView`'s `timeString` logic) and
 * backspace/long-press-clear reverse it. Replaces `CountdownTimeController`'s custom
 * `TimerPickerView`.
 */
@Composable
fun CountdownTimeValueEditor(
  builderItem: BuilderItem<Long>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var timeString by remember(builderItem) {
    mutableStateOf(
      builderItem.modifier.getValue()?.let {
        DateTimeManager.generateViewAfterString(it, divider = "")
      } ?: EMPTY_TIME_STRING,
    )
  }

  fun commit(newTimeString: String) {
    timeString = newTimeString
    val millis = SuperUtil.getAfterTime(newTimeString)
    builderItem.modifier.update(if (millis == 0L) null else millis)
    onValueChange(builderItem)
  }

  fun onDigit(digit: Int) {
    if (timeString[0] == '0') {
      commit(timeString.substring(1) + digit)
    }
  }

  fun onBackspace() {
    commit("0" + timeString.substring(0, timeString.length - 1))
  }

  fun onClear() {
    commit(EMPTY_TIME_STRING)
  }

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "${timeString.substring(0, 2)}:${timeString.substring(2, 4)}:${timeString.substring(4, 6)}",
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
    }

    Column(
      modifier = Modifier.padding(top = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      KEYPAD_ROWS.forEach { row ->
        Row(horizontalArrangement = Arrangement.Center) {
          row.forEach { digit -> KeypadKey(text = digit.toString(), onClick = { onDigit(digit) }) }
        }
      }
      Row(horizontalArrangement = Arrangement.Center) {
        KeypadKey(text = "", onClick = {}, enabled = false)
        KeypadKey(text = "0", onClick = { onDigit(0) })
        KeypadKey(
          text = "⌫",
          onClick = ::onBackspace,
          onLongClick = ::onClear,
          enabled = timeString != EMPTY_TIME_STRING,
        )
      }
    }
  }
}

private const val KEY_SIZE_DP = 64

@Composable
private fun KeypadKey(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
  enabled: Boolean = true,
) {
  Column(
    modifier = modifier
      .padding(4.dp)
      .size(KEY_SIZE_DP.dp)
      .clip(CircleShape)
      .combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClick = onClick,
        onLongClick = onLongClick,
      ),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    if (text.isNotEmpty()) {
      Text(text = text, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}
