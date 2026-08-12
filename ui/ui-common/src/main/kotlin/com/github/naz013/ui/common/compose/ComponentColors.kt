package com.github.naz013.ui.common.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

/**
 * Provides consistent TopAppBar colors following Material 3 design.
 *
 * Returns a [TopAppBarColors] configuration with:
 * - Container color: primaryContainer
 * - Title content color: onPrimaryContainer
 *
 * This composable property can be used globally across the app
 * to ensure consistent styling of TopAppBars.
 *
 * Usage:
 * ```
 * TopAppBar(
 *   title = { Text("Title") },
 *   colors = TopAppbarColor
 * )
 * ```
 *
 * @return [TopAppBarColors] configured with the app's theme colors
 */
val TopAppbarColor: TopAppBarColors
  @Composable
  get() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    titleContentColor = MaterialTheme.colorScheme.onBackground
  )
