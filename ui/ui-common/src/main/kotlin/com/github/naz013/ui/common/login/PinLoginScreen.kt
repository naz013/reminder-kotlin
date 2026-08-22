package com.github.naz013.ui.common.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.PinInput

@Composable
internal fun PinLoginScreen(
  pin: String,
  shuffleDigits: Boolean,
  showFingerprintButton: Boolean,
  onDigitClick: (Int) -> Unit,
  onDeleteClick: () -> Unit,
  onFingerprintClick: () -> Unit,
  onCloseClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
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
        shuffleDigits = shuffleDigits,
        fingerprintButton = if (showFingerprintButton) {
          {
            TooltipIconButton(contentDescription = stringResource(R.string.enter_your_pin)) {
              IconButton(onClick = onFingerprintClick) {
                Icon(
                  painter = painterResource(R.drawable.ic_fluent_fingerprint),
                  contentDescription = stringResource(R.string.enter_your_pin),
                  tint = MaterialTheme.colorScheme.onBackground,
                )
              }
            }
          }
        } else {
          null
        },
      )
      Spacer(modifier = Modifier.weight(1.4f))
    }

    TooltipIconButton(
      contentDescription = stringResource(R.string.cd_back),
      modifier = Modifier
        .align(Alignment.TopStart)
        .statusBarsPadding()
        .padding(8.dp),
    ) {
      IconButton(onClick = onCloseClick) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_dismiss),
          contentDescription = stringResource(R.string.cd_back),
          tint = MaterialTheme.colorScheme.onBackground,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PinLoginScreenPreview() {
  AppTheme {
    PinLoginScreen(
      pin = "123",
      shuffleDigits = false,
      showFingerprintButton = true,
      onDigitClick = {},
      onDeleteClick = {},
      onFingerprintClick = {},
      onCloseClick = {},
    )
  }
}
