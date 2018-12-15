package com.elementary.tasks.core.di

import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.BaseNotificationActivity
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.additional.FollowReminderActivity
import com.elementary.tasks.core.appWidgets.WidgetDataProvider
import com.elementary.tasks.core.appWidgets.calendar.CalendarMonthFactory
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateMinusService
import com.elementary.tasks.core.appWidgets.calendar.CalendarUpdateService
import com.elementary.tasks.core.appWidgets.calendar.CalendarWeekdayFactory
import com.elementary.tasks.core.appWidgets.notes.NotesFactory
import com.elementary.tasks.core.appWidgets.tasks.TasksFactory
import com.elementary.tasks.core.appWidgets.voiceControl.VoiceWidgetDialog
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.async.BackupSettingTask
import com.elementary.tasks.core.async.BackupTask
import com.elementary.tasks.core.async.SyncTask
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
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.notes.BaseNotesViewModel
import com.elementary.tasks.core.viewModels.reminders.BaseRemindersViewModel
import com.elementary.tasks.core.views.*
import com.elementary.tasks.dayView.DayViewProvider
import com.elementary.tasks.dayView.day.EventsListFragment
import com.elementary.tasks.googleTasks.create.TaskListActivity
import com.elementary.tasks.groups.CreateGroupActivity
import com.elementary.tasks.login.LoginActivity
import com.elementary.tasks.navigation.MainActivity
import com.elementary.tasks.navigation.fragments.BaseFragment
import com.elementary.tasks.navigation.settings.BaseCalendarFragment
import com.elementary.tasks.navigation.settings.export.FragmentCloudDrives
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.editor.ImageEditActivity
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.list.NotesFragment
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.places.google.LocationPlacesAdapter
import com.elementary.tasks.places.list.PlacesRecyclerAdapter
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.createEdit.fragments.TypeFragment
import com.elementary.tasks.reminder.lists.ArchiveFragment
import com.elementary.tasks.reminder.lists.RemindersFragment
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
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
    fun inject(adapter: RemindersRecyclerAdapter)
    fun inject(eventManager: EventManager)
    fun inject(callReceiver: CallReceiver)
    fun inject(conversationAdapter: ConversationAdapter)
    fun inject(reminderHolder: ReminderHolder)
    fun inject(shoppingHolder: ShoppingHolder)
    fun inject(GTasks: GTasks)
    fun inject(google: GDrive)
    fun inject(backupTool: BackupTool)
    fun inject(baseDialog: BaseDialog)
    fun inject(themedActivity: ThemedActivity)
    fun inject(activity: CreateNoteActivity)
    fun inject(repeatNotificationReceiver: RepeatNotificationReceiver)
    fun inject(reminderUtils: ReminderUtils)
    fun inject(birthdayHolder: BirthdayHolder)
    fun inject(baseFragment: BaseFragment)
    fun inject(baseFragment: BaseCalendarFragment)
    fun inject(fragment: ArchiveFragment)
    fun inject(fragment: RemindersFragment)
    fun inject(fragment: NotesFragment)
    fun inject(calendarMonthFactory: CalendarMonthFactory)
    fun inject(widgetDataProvider: WidgetDataProvider)
    fun inject(calendarUpdateMinusService: CalendarUpdateMinusService)
    fun inject(calendarUpdateService: CalendarUpdateService)
    fun inject(calendarWeekdayFactory: CalendarWeekdayFactory)
    fun inject(syncTask: SyncTask)
    fun inject(backupSettingTask: BackupSettingTask)
    fun inject(backupTask: BackupTask)
    fun inject(notesFactory: NotesFactory)
    fun inject(tasksFactory: TasksFactory)
    fun inject(dropbox: Dropbox)
    fun inject(contactsRecyclerAdapter: ContactsRecyclerAdapter)
    fun inject(locationTracker: LocationTracker)
    fun inject(baseMapFragment: BaseMapFragment)
    fun inject(baseBroadcast: BaseBroadcast)
    fun inject(geolocationService: GeolocationService)
    fun inject(eventJobService: EventJobService)
    fun inject(themedImageButton: ThemedImageButton)
    fun inject(textViewWithIcon: TextViewWithIcon)
    fun inject(repeatView: RepeatView)
    fun inject(iconRadioButton: IconRadioButton)
    fun inject(monthView: MonthView)
    fun inject(dateTimeView: DateTimeView)
    fun inject(fragment: FragmentCloudDrives)
    fun inject(baseHolder: BaseHolder)
    fun inject(holder: NoteHolder)
    fun inject(imagesGridAdapter: ImagesGridAdapter)
    fun inject(typeFragment: TypeFragment)
    fun inject(fragment: EventsListFragment)
    fun inject(placesRecyclerAdapter: PlacesRecyclerAdapter)
    fun inject(locationPlacesAdapter: LocationPlacesAdapter)
    fun inject(voiceWidgetDialog: VoiceWidgetDialog)
    fun inject(dayViewProvider: DayViewProvider)
    fun inject(activity: MainActivity)
    fun inject(activity: CreateReminderActivity)
    fun inject(activity: BaseNotificationActivity)
    fun inject(activity: ReminderPreviewActivity)
    fun inject(activity: ImagePreviewActivity)
    fun inject(activity: ImageEditActivity)
    fun inject(activity: NotePreviewActivity)
    fun inject(activity: AddBirthdayActivity)
    fun inject(activity: CreateGroupActivity)
    fun inject(activity: FollowReminderActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: TaskListActivity)
}
