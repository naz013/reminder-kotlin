package com.elementary.tasks.core.di

import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.IntentActivity
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.appWidgets.WidgetDataProvider
import com.elementary.tasks.core.appWidgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.core.appWidgets.calendar.CalendarMonthFactory
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateMinusService
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateService
import com.elementary.tasks.core.appWidgets.calendar.CalendarWeekdayFactory
import com.elementary.tasks.core.appWidgets.events.EventsFactory
import com.elementary.tasks.core.appWidgets.googleTasks.TasksFactory
import com.elementary.tasks.core.appWidgets.notes.NotesFactory
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.cloud.Dropbox
import com.elementary.tasks.core.cloud.GDrive
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.contacts.ContactsRecyclerAdapter
import com.elementary.tasks.core.controller.EventManager
import com.elementary.tasks.core.dialogs.BaseDialog
import com.elementary.tasks.core.fragments.BaseMapFragment
import com.elementary.tasks.core.location.LocationTracker
import com.elementary.tasks.core.services.*
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ReminderUtils
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.conversation.ConversationViewModel
import com.elementary.tasks.core.view_models.google_tasks.BaseTaskListsViewModel
import com.elementary.tasks.core.view_models.notes.BaseNotesViewModel
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.core.views.RepeatView
import com.elementary.tasks.core.work.BackupDataWorker
import com.elementary.tasks.core.work.BackupSettingsWorker
import com.elementary.tasks.dayView.DayViewProvider
import com.elementary.tasks.dayView.day.EventsListFragment
import com.elementary.tasks.googleTasks.create.TaskActivity
import com.elementary.tasks.googleTasks.create.TaskListActivity
import com.elementary.tasks.googleTasks.list.TaskListFragment
import com.elementary.tasks.groups.CreateGroupActivity
import com.elementary.tasks.login.LoginActivity
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.navigation.settings.export.ExportSettingsFragment
import com.elementary.tasks.navigation.settings.export.FragmentCloudDrives
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.editor.ImageEditActivity
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.list.NotesFragment
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.create.fragments.TypeFragment
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
import com.elementary.tasks.voice.ConversationAdapter
import dagger.Component
import javax.inject.Singleton

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Singleton
@Component(modules = [
    AppModule::class,
    DbModule::class,
    UtilModule::class
])
interface AppComponent {
    fun language(): Language

    fun inject(viewModel: BaseDbViewModel)
    fun inject(viewModel: BaseNotesViewModel)
    fun inject(viewModel: BaseRemindersViewModel)
    fun inject(viewModel: ConversationViewModel)
    fun inject(viewModel: BaseTaskListsViewModel)

    fun inject(conversationAdapter: ConversationAdapter)
    fun inject(contactsRecyclerAdapter: ContactsRecyclerAdapter)
    fun inject(locationPlacesAdapter: LocationPlacesAdapter)

    fun inject(baseHolder: BaseHolder)
    fun inject(holder: NoteHolder)
    fun inject(birthdayHolder: BirthdayHolder)

    fun inject(eventManager: EventManager)
    fun inject(callReceiver: CallReceiver)
    fun inject(GTasks: GTasks)
    fun inject(google: GDrive)
    fun inject(backupTool: BackupTool)
    fun inject(baseDialog: BaseDialog)
    fun inject(repeatNotificationReceiver: RepeatNotificationReceiver)
    fun inject(reminderUtils: ReminderUtils)
    fun inject(calendarMonthFactory: CalendarMonthFactory)
    fun inject(widgetDataProvider: WidgetDataProvider)
    fun inject(calendarUpdateMinusService: CalendarUpdateMinusService)
    fun inject(calendarUpdateService: CalendarUpdateService)
    fun inject(calendarWeekdayFactory: CalendarWeekdayFactory)
    fun inject(worker: BackupSettingsWorker)
    fun inject(worker: BackupDataWorker)
    fun inject(notesFactory: NotesFactory)
    fun inject(tasksFactory: TasksFactory)
    fun inject(factory: EventsFactory)
    fun inject(dropbox: Dropbox)
    fun inject(locationTracker: LocationTracker)
    fun inject(baseBroadcast: BaseBroadcast)
    fun inject(geolocationService: GeolocationService)
    fun inject(eventJobService: EventJobService)
    fun inject(voiceWidgetDialog: VoiceWidgetDialog)
    fun inject(dayViewProvider: DayViewProvider)

    fun inject(baseFragment: BaseFragment)
    fun inject(baseFragment: BaseCalendarFragment)
    fun inject(fragment: NotesFragment)
    fun inject(baseMapFragment: BaseMapFragment)
    fun inject(fragment: FragmentCloudDrives)
    fun inject(typeFragment: TypeFragment)
    fun inject(fragment: EventsListFragment)
    fun inject(fragment: TaskListFragment)
    fun inject(fragment: ExportSettingsFragment)
    fun inject(fragment: BaseSettingsFragment)

    fun inject(repeatView: RepeatView)
    fun inject(monthView: MonthView)
    fun inject(dateTimeView: DateTimeView)

    fun inject(activity: ThemedActivity)
    fun inject(activity: CreateNoteActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: CreateReminderActivity)
    fun inject(activity: BaseNotificationActivity)
    fun inject(activity: ReminderPreviewActivity)
    fun inject(activity: ImagePreviewActivity)
    fun inject(activity: ImageEditActivity)
    fun inject(activity: NotePreviewActivity)
    fun inject(activity: AddBirthdayActivity)
    fun inject(activity: CreateGroupActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: TaskListActivity)
    fun inject(activity: TaskActivity)
    fun inject(activity: IntentActivity)
}
