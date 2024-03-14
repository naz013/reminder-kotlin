package com.elementary.tasks.reminder.build.valuedialog.controller.extra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.databinding.BuilderItemOtherParamsBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.OtherParams
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class OtherParamsController(
  builderItem: BuilderItem<OtherParams>
) : AbstractBindingValueController<OtherParams, BuilderItemOtherParamsBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemOtherParamsBinding {
    return BuilderItemOtherParamsBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.defaultSwitch.setOnCheckedChangeListener { _, isChecked ->
      updateEnabledState(!isChecked)
      updateValue(createParams())
    }

    val onCheckedChangeListener = { _: View, _: Boolean ->
      updateValue(createParams())
    }
    binding.ttsSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
    binding.vibrateSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
    binding.repeatNotificationSwitch.setOnCheckedChangeListener(onCheckedChangeListener)
  }

  override fun onDataChanged(data: OtherParams?) {
    super.onDataChanged(data)
    data?.also {
      binding.defaultSwitch.isChecked = it.useGlobal
      binding.ttsSwitch.isChecked = it.notifyByVoice
      binding.vibrateSwitch.isChecked = it.vibrate
      binding.repeatNotificationSwitch.isChecked = it.repeatNotification
    }
  }

  private fun updateEnabledState(enabled: Boolean) {
    binding.ttsSwitch.isEnabled = enabled
    binding.vibrateSwitch.isEnabled = enabled
    binding.repeatNotificationSwitch.isEnabled = enabled
  }

  private fun createParams(): OtherParams {
    return if (binding.defaultSwitch.isChecked) {
      OtherParams(useGlobal = true)
    } else {
      OtherParams(
        useGlobal = false,
        vibrate = binding.vibrateSwitch.isChecked,
        notifyByVoice = binding.ttsSwitch.isChecked,
        repeatNotification = binding.repeatNotificationSwitch.isChecked
      )
    }
  }
}
