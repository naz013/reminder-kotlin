package com.elementary.tasks.home

import com.elementary.tasks.home.scheduleview.ReminderGoogleTaskLiveData
import com.elementary.tasks.home.scheduleview.ReminderNoteLiveData
import com.elementary.tasks.home.scheduleview.ScheduleHomeViewModel
import com.elementary.tasks.home.scheduleview.ScheduleLiveData
import com.elementary.tasks.home.scheduleview.data.UiBirthdayScheduleListAdapter
import com.elementary.tasks.home.scheduleview.data.UiReminderScheduleListAdapter
import com.github.naz013.domain.Reminder
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
  factory { ReminderGoogleTaskLiveData(get(), get(), get(), get(), get()) }
  factory { ScheduleLiveData(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
  factory { (reminders: List<Reminder>) ->
    ReminderNoteLiveData(get(), get(), get(), reminders, get())
  }

  factory { UiReminderScheduleListAdapter(get(), get(), get(), get(), get(), get()) }
  factory { UiBirthdayScheduleListAdapter(get(), get(), get(), get(), get(), get()) }

  viewModel {
    ScheduleHomeViewModel(get(), get(), get())
  }
}
