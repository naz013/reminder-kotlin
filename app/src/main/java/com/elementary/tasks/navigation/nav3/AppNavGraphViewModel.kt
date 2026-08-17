package com.elementary.tasks.navigation.nav3

import androidx.lifecycle.ViewModel
import com.github.naz013.logic.workflow.WorkflowConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Computes app-shell-level configuration for [AppNavGraph] - today just which optional
 * top-level rail sections should be shown (Workflow is gated behind [WorkflowConfig] while that
 * feature isn't fully rolled out). [AppNavGraph] only reads [state]; it never touches
 * [WorkflowConfig] or any other config source directly, keeping that Composable a thin renderer
 * per this project's convention of pushing branching/config decisions into a ViewModel.
 */
class AppNavGraphViewModel : ViewModel() {
  private val _state = MutableStateFlow(AppNavGraphState(isWorkflowEnabled = WorkflowConfig.isEnabled))
  val state: StateFlow<AppNavGraphState> = _state.asStateFlow()
}

data class AppNavGraphState(
  val isWorkflowEnabled: Boolean = false,
)
