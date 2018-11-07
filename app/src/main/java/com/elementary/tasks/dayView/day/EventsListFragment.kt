package com.elementary.tasks.dayView.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.viewModels.dayVew.DayViewViewModel
import com.elementary.tasks.dayView.EventsPagerItem
import com.elementary.tasks.reminder.ReminderResolver
import kotlinx.android.synthetic.main.fragment_events_list.*
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
class EventsListFragment : Fragment() {

    private val mAdapter = CalendarEventsAdapter()
    private lateinit var viewModel: DayViewViewModel
    private val birthdayResolver = BirthdayResolver(deleteAction = { birthday -> viewModel.deleteBirthday(birthday) })
    private val reminderResolver = ReminderResolver(dialogAction = { return@ReminderResolver dialogues},
            saveAction = {reminder -> viewModel.saveReminder(reminder) },
            toggleAction = {},
            deleteAction = {reminder -> viewModel.moveToTrash(reminder) },
            allGroups = { return@ReminderResolver viewModel.allGroups.value ?: listOf() })
    private var mItem: EventsPagerItem? = null
    @Inject
    lateinit var dialogues: Dialogues

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun getModel(): EventsPagerItem? = mItem

    fun setModel(eventsPagerItem: EventsPagerItem) {
        this.mItem = eventsPagerItem
        try {
            viewModel.setItem(eventsPagerItem)
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mItem = arguments?.getSerializable(ARGUMENT_PAGE_NUMBER) as EventsPagerItem?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events_list, container, false)
    }

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

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter

        reloadView()
        initBirthdayViewModel()
    }

    private fun initBirthdayViewModel() {
        viewModel = ViewModelProviders.of(this).get(DayViewViewModel::class.java)
        viewModel.setItem(mItem)
        viewModel.events.observe(this, Observer<List<EventModel>> {
            if (it != null) {
                mAdapter.setData(it)
                reloadView()
            }
        })
    }

    private fun reloadView() {
        if (mAdapter.itemCount > 0) {
            recyclerView.visibility = View.VISIBLE
            emptyItem.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
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
