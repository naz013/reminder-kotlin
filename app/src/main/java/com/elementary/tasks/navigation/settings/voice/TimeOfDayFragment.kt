package com.elementary.tasks.navigation.settings.voice

import android.os.Bundle
import android.view.View
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.toDate
import com.elementary.tasks.core.utils.toHm
import com.elementary.tasks.databinding.FragmentSettingsTimeOfDayBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimeOfDayFragment : BaseSettingsFragment<FragmentSettingsTimeOfDayBinding>(), View.OnClickListener {

  private val viewModel by viewModel<TimesViewModel>()
  private var is24: Boolean = false
  private val format = SimpleDateFormat("HH:mm", Locale.getDefault())

  override fun layoutRes(): Int = R.layout.fragment_settings_time_of_day

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.nightTime.setOnClickListener(this)
    binding.eveningTime.setOnClickListener(this)
    binding.dayTime.setOnClickListener(this)
    binding.morningTime.setOnClickListener(this)

    is24 = prefs.is24HourFormat

    initMorningTime()
    initNoonTime()
    initEveningTime()
    initNightTime()
  }

  override fun onStart() {
    super.onStart()
    viewModel.morningTime.observe(viewLifecycleOwner, {
      if (it != null) {
        binding.morningTime.text = TimeUtil.getTime(it.toDate(), is24, prefs.appLanguage)
      }
    })
    viewModel.dayTime.observe(viewLifecycleOwner, {
      if (it != null) {
        binding.dayTime.text = TimeUtil.getTime(it.toDate(), is24, prefs.appLanguage)
      }
    })
    viewModel.eveningTime.observe(viewLifecycleOwner, {
      if (it != null) {
        binding.eveningTime.text = TimeUtil.getTime(it.toDate(), is24, prefs.appLanguage)
      }
    })
    viewModel.nightTime.observe(viewLifecycleOwner, {
      if (it != null) {
        binding.nightTime.text = TimeUtil.getTime(it.toDate(), is24, prefs.appLanguage)
      }
    })
  }

  private fun initNoonTime() {
    val noonTime = prefs.noonTime
    var date: Date? = null
    try {
      date = format.parse(noonTime)
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    viewModel.dayTime.postValue(date.toHm())
  }

  private fun initEveningTime() {
    val evening = prefs.eveningTime
    var date: Date? = null
    try {
      date = format.parse(evening)
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    viewModel.eveningTime.postValue(date.toHm())
  }

  private fun initNightTime() {
    val night = prefs.nightTime
    var date: Date? = null
    try {
      date = format.parse(night)
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    viewModel.nightTime.postValue(date.toHm())
  }

  private fun initMorningTime() {
    val morning = prefs.morningTime
    var date: Date? = null
    try {
      date = format.parse(morning)
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    viewModel.morningTime.postValue(date.toHm())
  }

  override fun getTitle(): String = getString(R.string.time)

  private fun morningDialog() {
    viewModel.morningTime.value?.let { (hour, minute) ->
      timeDialog(hour, minute) { hourOfDay, minuteOfHour ->
        val hm = TimeUtil.HM(hour = hourOfDay, minute = minuteOfHour)
        prefs.morningTime = format.format(hm.toDate())
        viewModel.morningTime.postValue(hm)
      }
    }
  }

  private fun dayDialog() {
    viewModel.dayTime.value?.let { (hour, minute) ->
      timeDialog(hour, minute) { hourOfDay, minuteOfHour ->
        val hm = TimeUtil.HM(hour = hourOfDay, minute = minuteOfHour)
        prefs.noonTime = format.format(hm.toDate())
        viewModel.dayTime.postValue(hm)
      }
    }
  }

  private fun nightDialog() {
    viewModel.nightTime.value?.let { (hour, minute) ->
      timeDialog(hour, minute) { hourOfDay, minuteOfHour ->
        val hm = TimeUtil.HM(hour = hourOfDay, minute = minuteOfHour)
        prefs.nightTime = format.format(hm.toDate())
        viewModel.nightTime.postValue(hm)
      }
    }
  }

  private fun eveningDialog() {
    viewModel.eveningTime.value?.let { (hour, minute) ->
      timeDialog(hour, minute) { hourOfDay, minuteOfHour ->
        val hm = TimeUtil.HM(hour = hourOfDay, minute = minuteOfHour)
        prefs.eveningTime = format.format(hm.toDate())
        viewModel.eveningTime.postValue(hm)
      }
    }
  }

  private fun timeDialog(h: Int, m: Int, callback: (hourOfDay: Int, minuteOfHour: Int) -> Unit) {
    withContext {
      TimeUtil.showTimePicker(it, prefs.is24HourFormat, h, m) { _, hourOfDay, minute ->
        callback.invoke(hourOfDay, minute)
      }
    }
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.morningTime -> morningDialog()
      R.id.dayTime -> dayDialog()
      R.id.eveningTime -> eveningDialog()
      R.id.nightTime -> nightDialog()
    }
  }
}
