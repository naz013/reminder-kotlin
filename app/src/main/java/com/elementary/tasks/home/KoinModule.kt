package com.elementary.tasks.home

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.home.scheduleview.ReminderGoogleTaskLiveData
import com.elementary.tasks.home.scheduleview.ReminderNoteLiveData
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.home.scheduleview.ScheduleLiveData
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  factory { ReminderGoogleTaskLiveData(get(), get(), get(), get()) }
  factory { ScheduleLiveData(get(), get(), get(), get(), get(), get(), get(), get()) }
  factory { (reminders: List<Reminder>) ->
    ReminderNoteLiveData(get(), get(), get(), reminders)
  }

  viewModel {
    ScheduleHomeViewModel(get(), get(), get())
  }
}
