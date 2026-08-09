package com.github.naz013.insights

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.github.naz013.insights.compose.InsightsScreen
import com.github.naz013.insights.compose.InsightsScreenState
import com.github.naz013.insights.compose.InsightsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.insightsEntries(backStack: MutableList<NavKey>) {
  entry<InsightsNavKey.Dashboard> { InsightsDashboardEntry(backStack) }
}

@Composable
private fun InsightsDashboardEntry(backStack: MutableList<NavKey>) {
  val viewModel = koinViewModel<InsightsViewModel>()

  val state by viewModel.state.collectAsState(InsightsScreenState())
  InsightsScreen(
    state = state,
    onBackClick = { backStack.removeLastOrNull() }
  )
}
