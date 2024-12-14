package com.elementary.tasks.reminder.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.applyBottomInsets
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.PrefsView
import com.elementary.tasks.databinding.ActivityConfigureReminderCreatorBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

@Deprecated("Replaced by new Builder")
class ConfigureActivity : BindingActivity<ActivityConfigureReminderCreatorBinding>() {

  private val gTasks by inject<GTasks>()
  private val config: ReminderCreatorConfig = prefs.reminderCreatorParams

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
}
