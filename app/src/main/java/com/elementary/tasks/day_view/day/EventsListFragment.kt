package com.elementary.tasks.day_view.day

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.core.BindingFragment
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentEventsListBinding
import com.elementary.tasks.day_view.EventsPagerItem
import com.elementary.tasks.reminder.ReminderResolver
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
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
class EventsListFragment : BindingFragment<FragmentEventsListBinding>() {

    private var callback: DayCallback? = null
    private val mAdapter = CalendarEventsAdapter()
    private val birthdayResolver = BirthdayResolver(deleteAction = { birthday -> callback?.getViewModel()?.deleteBirthday(birthday) })
    private val reminderResolver = ReminderResolver(dialogAction = { return@ReminderResolver dialogues},
            saveAction = {reminder -> callback?.getViewModel()?.saveReminder(reminder) },
            toggleAction = {},
            deleteAction = {reminder -> callback?.getViewModel()?.moveToTrash(reminder) },
            allGroups = { return@ReminderResolver callback?.getViewModel()?.groups ?: listOf() })
    private var mItem: EventsPagerItem? = null
    @Inject
    lateinit var dialogues: Dialogues

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun getModel(): EventsPagerItem? = mItem

    fun setModel(eventsPagerItem: EventsPagerItem) {
        mAdapter.setData(listOf())
        this.mItem = eventsPagerItem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = parentFragment
        if (fragment != null) {
            callback = fragment as DayCallback?
        }
        if (arguments != null) {
            mItem = arguments?.getSerializable(ARGUMENT_PAGE_NUMBER) as EventsPagerItem?
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_events_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter.setEventListener(object : ActionsListener<EventModel> {
            override fun onAction(view: View, position: Int, t: EventModel?, actions: ListActions) {
                if (t == null) return
                val item = t.model
                if (item is Birthday) {
                    birthdayResolver.resolveAction(view, item, actions)
                } else if (item is Reminder) {
                    reminderResolver.resolveAction(view, item, actions)
                }
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = mAdapter

        reloadView()
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyItem.visibility = View.GONE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyItem.visibility = View.VISIBLE
        }
    }

    fun requestData() {
        val item = mItem
        if (item != null) {
            launchDefault {
                delay(250)
                withUIContext {
                    callback?.find(item) { eventsPagerItem, list ->
                        Timber.d("setModel: $eventsPagerItem, ${list.size}")
                        mAdapter.setData(list)
                        reloadView()
                    }
                }
            }

        }
    }

    companion object {
        private const val ARGUMENT_PAGE_NUMBER = "arg_page"
        fun newInstance(item: EventsPagerItem): EventsListFragment {
            val pageFragment = EventsListFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARGUMENT_PAGE_NUMBER, item)
            pageFragment.arguments = bundle
            return pageFragment
        }
    }
}
