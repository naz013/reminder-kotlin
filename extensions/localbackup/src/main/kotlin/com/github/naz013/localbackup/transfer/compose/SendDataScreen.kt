package com.github.naz013.localbackup.transfer.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.localbackup.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

@Composable
internal fun SendDataScreen(
  modifier: Modifier = Modifier,
  state: SendDataState,
  onDoneClick: () -> Unit,
) {
  AnimatedGradientBackground(modifier = modifier) {
    Column(modifier = Modifier.fillMaxSize()) {
      GradientScreenHeader(
        onBackClick = onDoneClick,
        contentDescription = stringResource(R.string.acc_close),
        icon = AppIcons.Fluent.Dismiss,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = stringResource(R.string.transfer_sending_title),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientHeroCard {
          TransferStatusContent(
            status = state.status,
            progressMessage = stringResource(R.string.transfer_sending_message),
            successMessageRes = R.string.transfer_sent_success,
            onDoneClick = onDoneClick,
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SendDataScreenInProgressPreview() {
  AppTheme {
    SendDataScreen(
      state = SendDataState(status = TransferStatus.InProgress),
      onDoneClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SendDataScreenSuccessPreview() {
  AppTheme {
    SendDataScreen(
      state = SendDataState(status = TransferStatus.Success(totalCount = 12)),
      onDoneClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun SendDataScreenErrorPreview() {
  AppTheme {
    SendDataScreen(
      state = SendDataState(status = TransferStatus.Error(messageRes = R.string.transfer_send_failed)),
      onDoneClick = {},
    )
  }
}
