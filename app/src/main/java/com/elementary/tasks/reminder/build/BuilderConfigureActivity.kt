package com.elementary.tasks.reminder.build

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.os.buildIntent
import com.elementary.tasks.core.os.datapicker.ActivityLauncherCreator
import com.elementary.tasks.core.os.datapicker.FragmentLauncherCreator
import com.elementary.tasks.core.os.datapicker.IntentPicker
import com.elementary.tasks.core.os.datapicker.LauncherCreator
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.applyBottomInsets
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.PrefsView
import com.elementary.tasks.databinding.ActivityConfigureReminderCreatorBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

class BuilderConfigureActivity : BindingActivity<ActivityConfigureReminderCreatorBinding>() {

  private val gTasks by inject<GTasks>()
  private val config: ReminderCreatorConfig = prefs.reminderCreatorParams

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

    binding.tasksParam.visibleGone(gTasks.isLogged)
    initParam(binding.tasksParam, config.isGoogleTasksPickerEnabled()) {
      config.setGoogleTasksPickerEnabled(it)
    }

    initParam(binding.extraParam, config.isTuneExtraPickerEnabled()) {
      config.setTuneExtraPickerEnabled(it)
    }

    binding.ledParam.visibleGone(Module.isPro)
    initParam(binding.ledParam, config.isLedPickerEnabled()) {
      config.setLedPickerEnabled(it)
    }

    binding.newBuilderSection.visible()

    binding.iCalendarParam.visibleGone(Module.isPro)
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
    Timber.d("save: $config")
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
