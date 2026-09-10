package com.github.naz013.localbackup.transfer.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.naz013.localbackup.R
import com.github.naz013.ui.common.compose.AppIcons

/**
 * The status body shown inside a [com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard]
 * on both transfer screens ([SendDataScreen] while preparing/waiting on the sibling app,
 * [TransferReceiveScreen] while importing) - same in-progress/success/error shape, only the
 * wording differs per direction.
 */
@Composable
internal fun ColumnScope.TransferStatusContent(
  status: TransferStatus,
  progressMessage: String,
  successMessageRes: Int,
  onDoneClick: () -> Unit,
) {
  when (status) {
    is TransferStatus.InProgress -> {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = progressMessage,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    is TransferStatus.Success -> {
      Icon(
        painter = AppIcons.Fluent.CheckmarkCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .size(64.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(successMessageRes, status.totalCount),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(onClick = onDoneClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.ok))
      }
    }

    is TransferStatus.Error -> {
      Icon(
        painter = AppIcons.Fluent.ErrorCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .size(64.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(status.messageRes),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(onClick = onDoneClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.ok))
      }
    }
  }
}
