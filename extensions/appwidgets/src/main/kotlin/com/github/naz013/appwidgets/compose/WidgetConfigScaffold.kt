package com.github.naz013.appwidgets.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton

/**
 * Shared `Scaffold`/`TopAppBar` shell (back action + Save action) reused by every widget
 * configuration screen - the same shape repeated across all 7 widget types today.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WidgetConfigScaffold(
  modifier: Modifier = Modifier,
  title: String,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  saveEnabled: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Fluent.Dismiss,
            contentDescription = stringResource(R.string.acc_close),
            onClick = onBackClick,
            modifier = Modifier.padding(4.dp)
          )
        },
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = saveEnabled,
            onClick = onSaveClick,
          )
        },
        colors = TopAppbarColor,
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      content = content,
    )
  }
}
