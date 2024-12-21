package com.elementary.tasks.settings.voice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.elementary.tasks.core.utils.ui.DateTimePickerProvider
import com.elementary.tasks.databinding.FragmentSettingsTimeOfDayBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimeOfDayFragment : BaseSettingsFragment<FragmentSettingsTimeOfDayBinding>() {

  private val viewModel by viewModel<TimesViewModel>()
  private val dateTimePickerProvider by inject<DateTimePickerProvider>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTimeOfDayBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.nightTime.setOnClickListener { nightDialog() }
    binding.eveningTime.setOnClickListener { eveningDialog() }
    binding.dayTime.setOnClickListener { dayDialog() }
    binding.morningTime.setOnClickListener { morningDialog() }

    viewModel.morningTime.nonNullObserve(viewLifecycleOwner) { binding.morningTime.text = it }
    viewModel.dayTime.nonNullObserve(viewLifecycleOwner) { binding.dayTime.text = it }
    viewModel.eveningTime.nonNullObserve(viewLifecycleOwner) { binding.eveningTime.text = it }
    viewModel.nightTime.nonNullObserve(viewLifecycleOwner) { binding.nightTime.text = it }
    viewModel.initTimes()
  }

  override fun getTitle(): String = getString(R.string.time)

  private fun morningDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.morningLocalTime,
      title = getString(R.string.morning)
    ) {
      viewModel.onMorningTime(it)
    }
  }

  private fun dayDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.dayLocalTime,
      title = getString(R.string.day)
    ) {
      viewModel.onDayTime(it)
    }
  }

  private fun nightDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.nightLocalTime,
      title = getString(R.string.night)
    ) {
      viewModel.onNightTime(it)
    }
  }

  private fun eveningDialog() {
    dateTimePickerProvider.showTimePicker(
      fragmentManager = childFragmentManager,
      time = viewModel.eveningLocalTime,
      title = getString(R.string.evening)
    ) {
      viewModel.onEveningTime(it)
    }
  }
}
