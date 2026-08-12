package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme

private val DigitButtonSize = 64.dp
private val DotSize = 20.dp
private val RowSpacing = 24.dp

/**
 * Reusable 0-9 PIN pad: a row of dot indicators showing how many digits have been entered, and a
 * numeric keypad below it. Fully stateless/controlled - the caller owns [pin] and reacts to
 * [onDigitClick]/[onDeleteClick], same as any other Compose input.
 *
 * [onDeleteClick] clears the whole entry rather than removing a single digit, matching the
 * behavior of the legacy `PinCodeView` this replaces.
 *
 * [shuffleDigits] re-randomizes which digit sits on which button every time [pin] changes - an
 * anti-shoulder-surfing option surfaced as a security setting on the PIN login screen. [
 * fingerprintButton], when non-null, is drawn in the slot reserved to the left of "0" (fixed size,
 * so layout doesn't shift when it's absent).
 */
@Composable
fun PinInput(
  pin: String,
  onDigitClick: (Int) -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
  pinLength: Int = 6,
  shuffleDigits: Boolean = false,
  fingerprintButton: (@Composable () -> Unit)? = null,
) {
  val digitOrder = remember(pin, shuffleDigits) {
    val order = (1..9).toMutableList().apply { add(0) }
    if (shuffleDigits) order.shuffle()
    order
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    PinDots(enteredCount = pin.length, pinLength = pinLength)
    Spacer(modifier = Modifier.size(RowSpacing))
    Row(horizontalArrangement = Arrangement.spacedBy(RowSpacing)) {
      PinDigitButton(digit = digitOrder[0], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[1], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[2], onClick = onDigitClick)
    }
    Spacer(modifier = Modifier.size(RowSpacing))
    Row(horizontalArrangement = Arrangement.spacedBy(RowSpacing)) {
      PinDigitButton(digit = digitOrder[3], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[4], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[5], onClick = onDigitClick)
    }
    Spacer(modifier = Modifier.size(RowSpacing))
    Row(horizontalArrangement = Arrangement.spacedBy(RowSpacing)) {
      PinDigitButton(digit = digitOrder[6], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[7], onClick = onDigitClick)
      PinDigitButton(digit = digitOrder[8], onClick = onDigitClick)
    }
    Spacer(modifier = Modifier.size(RowSpacing))
    Row(
      horizontalArrangement = Arrangement.spacedBy(RowSpacing),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(modifier = Modifier.size(DigitButtonSize)) {
        fingerprintButton?.invoke()
      }
      PinDigitButton(digit = digitOrder[9], onClick = onDigitClick)
      IconButton(onClick = onDeleteClick, modifier = Modifier.size(DigitButtonSize)) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_dismiss),
          contentDescription = stringResource(R.string.delete),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun PinDots(enteredCount: Int, pinLength: Int) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    for (index in 0 until pinLength) {
      if (index > 0) {
        Spacer(modifier = Modifier.width(if (index == pinLength / 2) 16.dp else 4.dp))
      }
      Icon(
        painter = painterResource(R.drawable.ic_fluent_text_asterisk),
        contentDescription = null,
        tint = if (index < enteredCount) {
          MaterialTheme.colorScheme.onBackground
        } else {
          MaterialTheme.colorScheme.onBackground.copy(alpha = 0f)
        },
        modifier = Modifier.size(DotSize),
      )
    }
  }
}

@Composable
private fun PinDigitButton(digit: Int, onClick: (Int) -> Unit) {
  Surface(
    onClick = { onClick(digit) },
    modifier = Modifier.size(DigitButtonSize),
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shadowElevation = 1.dp,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = digit.toString(),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PinInputPreview() {
  AppTheme {
    PinInput(
      pin = "12",
      onDigitClick = {},
      onDeleteClick = {},
    )
  }
}
