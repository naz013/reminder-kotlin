package com.elementary.tasks.reminder.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.views.PrefsView
import com.elementary.tasks.databinding.ActivityConfigureReminderCreatorBinding
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyTopInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject

@Deprecated("Replaced by new Builder")
class ConfigureActivity : BindingActivity<ActivityConfigureReminderCreatorBinding>() {

  private val prefs by inject<Prefs>()
  private val googleTasksAuthManager by inject<GoogleTasksAuthManager>()
  private val config: ReminderCreatorConfig by lazy { prefs.reminderCreatorParams }

  override fun inflateBinding() = ActivityConfigureReminderCreatorBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    binding.appBar.applyTopInsets()
    binding.scrollView.applyBottomInsets()
    binding.toolbar.setNavigationOnClickListener { finish() }

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
}
