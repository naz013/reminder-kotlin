package com.github.naz013.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
  modifier: Modifier = Modifier,
  title: String,
  onBackClick: () -> Unit,
  navigationIcon: Int = R.drawable.ic_builder_arrow_left,
  navigationContentDescription: String = stringResource(R.string.cd_back),
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          MenuIconButton(
            icon = painterResource(navigationIcon),
            contentDescription = navigationContentDescription,
            onClick = onBackClick,
          )
        },
        colors = TopAppbarColor,
      )
    },
    content = content,
  )
}

/**
 * Picks the leading icon for a settings screen: a close (X) icon when there's nothing meaningful
 * to "go back" to within the screen itself - either because it was opened directly with a
 * caller-supplied [screenTitle] (bypassing the Hub), or because it's rendered as a two-pane
 * detail pane alongside the Hub's list pane ([renderAsDetailPane]) - and a normal back arrow
 * otherwise. [onBackClick] pops the entry either way.
 */
fun settingsNavigationIcon(screenTitle: String? = null, renderAsDetailPane: Boolean = false): Int =
  if (screenTitle != null || renderAsDetailPane) R.drawable.ic_builder_clear else R.drawable.ic_builder_arrow_left

/** Content description matching [settingsNavigationIcon]'s icon choice for the same arguments. */
@Composable
fun settingsNavigationContentDescription(screenTitle: String? = null, renderAsDetailPane: Boolean = false): String =
  if (screenTitle != null || renderAsDetailPane) {
    stringResource(R.string.acc_close)
  } else {
    stringResource(R.string.cd_back)
  }
