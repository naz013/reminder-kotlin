package com.elementary.tasks.dayView

import android.content.Intent
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
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.viewModels.dayVew.DayViewViewModel
import com.elementary.tasks.reminder.createEdit.CreateReminderActivity
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity
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
    private var mItem: EventsPagerItem? = null
    @Inject
    lateinit var dialogues: Dialogues

    init {
        ReminderApp.appComponent.inject(this)
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
                when (actions) {
                    ListActions.MORE -> {
                        val model = t.model
                        if (model is Birthday) {
                            showBirthdayLcam(view, model)
                        }
                    }
                    ListActions.OPEN -> {
                        val model = t.model
                        if (model is Birthday) {
                            editBirthday(model)
                        } else if (model is Reminder) {
                            if (view.id == R.id.button_more) {
                                showActionDialog(model, view)
                            } else {
                                showReminder(model)
                            }
                        }
                    }
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

    private fun showBirthdayLcam(view: View, birthday: Birthday) {
        val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
        dialogues.showPopup(context!!, view, { item ->
            when (item) {
                0 -> editBirthday(birthday)
                1 -> viewModel.deleteBirthday(birthday)
            }
        }, *items)
    }

    private fun editBirthday(item: Birthday) {
        startActivity(Intent(context, AddBirthdayActivity::class.java)
                .putExtra(Constants.INTENT_ID, item.uuId))
    }

    private fun showReminder(reminder: Reminder) {
        startActivity(Intent(context, ReminderPreviewActivity::class.java)
                .putExtra(Constants.INTENT_ID, reminder.uuId))
    }

    private fun editReminder(uuId: String?) {
        startActivity(Intent(context, CreateReminderActivity::class.java)
                .putExtra(Constants.INTENT_ID, uuId))
    }

    private fun showActionDialog(reminder: Reminder, view: View) {
        val items = arrayOf(getString(R.string.open), getString(R.string.edit), getString(R.string.move_to_trash))
        dialogues.showPopup(context!!, view, { item ->
            when (item) {
                0 -> showReminder(reminder)
                1 -> editReminder(reminder.uuId)
                3 -> viewModel.moveToTrash(reminder)
            }
        }, *items)
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
