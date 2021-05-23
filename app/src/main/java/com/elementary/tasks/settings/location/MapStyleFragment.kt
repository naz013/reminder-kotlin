package com.elementary.tasks.settings.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsMapStyleBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class MapStyleFragment : BaseSettingsFragment<FragmentSettingsMapStyleBinding>() {

  private val selection: Int
    get() {
      return when {
        binding.styleAuto.isChecked -> 6
        binding.styleDay.isChecked -> 0
        binding.styleRetro.isChecked -> 1
        binding.styleSilver.isChecked -> 2
        binding.styleNight.isChecked -> 3
        binding.styleDark.isChecked -> 4
        binding.styleAubergine.isChecked -> 5
        else -> 0
      }
    }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsMapStyleBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.styleDay.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleAubergine.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleAuto.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleDark.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleNight.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleRetro.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }
    binding.styleSilver.setOnCheckedChangeListener { buttonView, isChecked -> invoke(buttonView, isChecked) }

    selectCurrent(prefs.mapStyle)
  }

  private fun selectCurrent(mapStyle: Int) {
    when (mapStyle) {
      1 -> binding.styleRetro.isChecked = true
      2 -> binding.styleSilver.isChecked = true
      3 -> binding.styleNight.isChecked = true
      4 -> binding.styleDark.isChecked = true
      5 -> binding.styleAubergine.isChecked = true
      6 -> binding.styleAuto.isChecked = true
      else -> binding.styleDay.isChecked = true
    }
  }

  private fun invoke(v: View, isChecked: Boolean) {
    if (!isChecked) return
    buttons().forEach {
      if (v.id != it.id) {
        it.isChecked = false
      }
    }
  }

  override fun onPause() {
    super.onPause()
    prefs.mapStyle = selection
  }

  private fun buttons(): List<AppCompatRadioButton> {
    return listOf(binding.styleDay, binding.styleAubergine, binding.styleAuto, binding.styleDark,
      binding.styleNight, binding.styleRetro, binding.styleSilver)
  }

  override fun getTitle(): String = getString(R.string.map_style)

  companion object {

    fun newInstance(): MapStyleFragment = MapStyleFragment()
  }
}
