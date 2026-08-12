package com.elementary.tasks.navigation.nav3

import com.elementary.tasks.BaseTest
import com.github.naz013.feature.workflow.WorkflowConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavGraphViewModelTest : BaseTest() {

  @Test
  fun `state reflects WorkflowConfig at construction`() {
    val viewModel = AppNavGraphViewModel()

    assertEquals(WorkflowConfig.isEnabled, viewModel.state.value.isWorkflowEnabled)
  }
}
