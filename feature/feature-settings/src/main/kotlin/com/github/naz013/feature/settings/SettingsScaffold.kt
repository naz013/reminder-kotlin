package com.github.naz013.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
  title: String,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: Int = R.drawable.ic_builder_arrow_left,
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
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
