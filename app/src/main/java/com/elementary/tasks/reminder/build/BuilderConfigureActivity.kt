package com.elementary.tasks.reminder.build

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator
import com.github.naz013.ui.common.activity.LightThemedActivity
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.context.buildIntent
import org.koin.androidx.viewmodel.ext.android.viewModel

class BuilderConfigureActivity : LightThemedActivity() {

  private val viewModel by viewModel<BuilderConfigureViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    composeView { Content() }
  }

  @Composable
  private fun Content() {
    val state by viewModel.state.collectAsState()
    BuilderConfigureScreen(
      state = state,
      onBackClick = ::closeScreen,
      onSummaryToggle = viewModel::onSummaryToggle,
      onBeforeToggle = viewModel::onBeforeToggle,
      onRepeatToggle = viewModel::onRepeatToggle,
      onRepeatLimitToggle = viewModel::onRepeatLimitToggle,
      onPriorityToggle = viewModel::onPriorityToggle,
      onAttachmentToggle = viewModel::onAttachmentToggle,
      onCalendarToggle = viewModel::onCalendarToggle,
      onTasksToggle = viewModel::onTasksToggle,
      onExtraToggle = viewModel::onExtraToggle,
      onLedToggle = viewModel::onLedToggle,
      onICalendarToggle = viewModel::onICalendarToggle,
      onMakeCallToggle = viewModel::onMakeCallToggle,
      onSendSmsToggle = viewModel::onSendSmsToggle,
      onOpenAppToggle = viewModel::onOpenAppToggle,
      onOpenLinkToggle = viewModel::onOpenLinkToggle,
      onSendEmailToggle = viewModel::onSendEmailToggle,
    )
  }

  private fun closeScreen() {
    setResult(RESULT_OK)
    finish()
  }

  override fun handleBackPress(): Boolean {
    closeScreen()
    return false
  }

  class BuilderConfigureLauncher private constructor(
    launcherCreator: LauncherCreator<Intent, ActivityResult>,
    private val resultCallback: () -> Unit,
  ) : IntentPicker<Intent, ActivityResult>(
      ActivityResultContracts.StartActivityForResult(),
      launcherCreator,
    ) {
    constructor(
      activity: ComponentActivity,
      resultCallback: () -> Unit,
    ) : this(ActivityLauncherCreator(activity), resultCallback)

    constructor(
      fragment: Fragment,
      resultCallback: () -> Unit,
    ) : this(FragmentLauncherCreator(fragment), resultCallback)

    fun configure() {
      launch(getIntent())
    }

    override fun dispatchResult(result: ActivityResult) {
      if (result.resultCode == Activity.RESULT_OK) {
        resultCallback()
      }
    }

    private fun getIntent(): Intent = getActivity().buildIntent(BuilderConfigureActivity::class.java)
  }
}
