package com.github.naz013.feature.settings.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.PinInput

@Composable
internal fun DisablePinScreen(
  pin: String,
  onDigitClick: (Int) -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = stringResource(R.string.enter_pin).uppercase(),
      style = MaterialTheme.typography.headlineMedium,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(48.dp))
    PinInput(
      pin = pin,
      onDigitClick = onDigitClick,
      onDeleteClick = onDeleteClick,
    )
    Spacer(modifier = Modifier.weight(1.4f))
  }
}

@Preview(showBackground = true)
@Composable
private fun DisablePinScreenPreview() {
  AppTheme {
    DisablePinScreen(
      pin = "123",
      onDigitClick = {},
      onDeleteClick = {},
    )
  }
}
