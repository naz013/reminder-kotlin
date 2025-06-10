package com.elementary.tasks.reminder.build

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.views.PrefsView
import com.elementary.tasks.databinding.ActivityConfigureReminderCreatorBinding
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.ui.common.view.visible
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject

class BuilderConfigureActivity : BindingActivity<ActivityConfigureReminderCreatorBinding>() {

  private val prefs by inject<Prefs>()
  private val googleTasksAuthManager by inject<GoogleTasksAuthManager>()
  private val config: ReminderCreatorConfig by lazy { prefs.reminderCreatorParams }

  override fun inflateBinding() = ActivityConfigureReminderCreatorBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    binding.appBar.applyTopInsets()
    binding.scrollView.applyBottomInsets()

    binding.toolbar.setNavigationOnClickListener { closeScreen() }

    initParam(binding.beforeParam, config.isBeforePickerEnabled()) {
      config.setBeforePickerEnabled(it)
    }

    initParam(binding.repeatParam, config.isRepeatPickerEnabled()) {
      config.setRepeatPickerEnabled(it)
    }

    initParam(binding.repeatLimitParam, config.isRepeatLimitPickerEnabled()) {
      config.setRepeatLimitPickerEnabled(it)
    }

    initParam(binding.priorityParam, config.isPriorityPickerEnabled()) {
      config.setPriorityPickerEnabled(it)
    }

    initParam(binding.attachmentParam, config.isAttachmentPickerEnabled()) {
      config.setAttachmentPickerEnabled(it)
    }

    initParam(binding.calendarParam, config.isCalendarPickerEnabled()) {
      config.setCalendarPickerEnabled(it)
    }

    binding.tasksParam.visibleGone(googleTasksAuthManager.isAuthorized())
    initParam(binding.tasksParam, config.isGoogleTasksPickerEnabled()) {
      config.setGoogleTasksPickerEnabled(it)
    }

    initParam(binding.extraParam, config.isTuneExtraPickerEnabled()) {
      config.setTuneExtraPickerEnabled(it)
    }

    binding.ledParam.visibleGone(BuildParams.isPro)
    initParam(binding.ledParam, config.isLedPickerEnabled()) {
      config.setLedPickerEnabled(it)
    }

    binding.newBuilderSection.visible()

    binding.iCalendarParam.visibleGone(BuildParams.isPro)
    initParam(binding.iCalendarParam, config.isICalendarEnabled()) {
      config.setICalendarEnabled(it)
    }

    initParam(binding.makeCallParam, config.isPhoneCallEnabled()) {
      config.setPhoneCallEnabled(it)
    }
    initParam(binding.sendSmsParam, config.isSendSmsEnabled()) {
      config.setSendSmsEnabled(it)
    }
    initParam(binding.openAppParam, config.isOpenAppEnabled()) {
      config.setOpenAppEnabled(it)
    }
    initParam(binding.openLinkParam, config.isOpenLinkEnabled()) {
      config.setOpenLinkEnabled(it)
    }
    initParam(binding.sendEmailParam, config.isSendEmailEnabled()) {
      config.setSendEmailEnabled(it)
    }

    initParam(binding.summaryParam, config.isAutoAddSummary()) {
      config.setAutoAddSummary(it)
    }
  }

  private fun initParam(prefsView: PrefsView, enabled: Boolean, onClick: (Boolean) -> Unit) {
    prefsView.isChecked = enabled
    prefsView.setOnClickListener {
      prefsView.isChecked = !prefsView.isChecked
      onClick(prefsView.isChecked)
      save()
    }
  }

  private fun save() {
    Logger.d("save: $config")
    prefs.reminderCreatorParams = config
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
    private val resultCallback: () -> Unit
  ) : IntentPicker<Intent, ActivityResult>(
    ActivityResultContracts.StartActivityForResult(),
    launcherCreator
  ) {

    constructor(
      activity: ComponentActivity,
      resultCallback: () -> Unit
    ) : this(ActivityLauncherCreator(activity), resultCallback)

    constructor(
      fragment: Fragment,
      resultCallback: () -> Unit
    ) : this(FragmentLauncherCreator(fragment), resultCallback)

    fun configure() {
      launch(getIntent())
    }

    override fun dispatchResult(result: ActivityResult) {
      if (result.resultCode == Activity.RESULT_OK) {
        resultCallback()
      }
    }

    private fun getIntent(): Intent {
      return getActivity().buildIntent(BuilderConfigureActivity::class.java)
    }
  }
}
