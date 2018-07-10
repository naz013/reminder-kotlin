package com.elementary.tasks.core.di

import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.controller.EventManager
import com.elementary.tasks.core.services.CallReceiver
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.reminder.lists.ReminderHolder
import com.elementary.tasks.reminder.lists.RemindersRecyclerAdapter
import com.elementary.tasks.reminder.lists.ShoppingHolder
import com.elementary.tasks.voice.ConversationAdapter

import javax.inject.Singleton

import dagger.Component

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
@Component(modules = arrayOf(AppModule::class, DbModule::class, ThemeModule::class))
interface AppComponent {
    fun inject(viewModel: BaseDbViewModel)

    fun inject(adapter: RemindersRecyclerAdapter)

    fun inject(eventManager: EventManager)

    fun inject(callReceiver: CallReceiver)

    fun inject(conversationAdapter: ConversationAdapter)

    fun inject(reminderHolder: ReminderHolder)

    fun inject(shoppingHolder: ShoppingHolder)

    fun inject(google: Google)

    fun inject(backupTool: BackupTool)
}
