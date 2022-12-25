package com.elementary.tasks.navigation.fragments

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.DialogActionPickerBinding
import com.elementary.tasks.day_view.DayViewFragment
import com.elementary.tasks.day_view.day.CalendarEventsAdapter
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.coroutines.Job
import timber.log.Timber
import java.util.Calendar

abstract class BaseCalendarFragment<B : ViewBinding> : BaseNavigationFragment<B>() {

  protected var dateMills: Long = 0
  private var mDialog: AlertDialog? = null
  private var job: Job? = null
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { }
  )
  private val reminderResolver = ReminderResolver(
    dialogAction = { dialogues },
    toggleAction = { },
    deleteAction = { },
    skipAction = { }
  )

  protected fun showActionDialog(showEvents: Boolean, list: List<EventModel> = listOf()) {
    withContext {
      val builder = dialogues.getMaterialDialog(it)
      val binding = DialogActionPickerBinding.inflate(LayoutInflater.from(it))
      binding.addBirth.setOnClickListener {
        mDialog?.dismiss()
        addBirthday()
      }
      binding.addBirth.setOnLongClickListener {
        showMessage(getString(R.string.add_birthday))
        true
      }
      binding.addEvent.setOnClickListener {
        mDialog?.dismiss()
        addReminder()
      }
      binding.addEvent.setOnLongClickListener {
        showMessage(getString(R.string.add_reminder_menu))
        true
      }
      if (showEvents && list.isNotEmpty()) {
        binding.loadingView.visibility = View.VISIBLE
        binding.eventsList.layoutManager = LinearLayoutManager(it)
        loadEvents(binding.eventsList, binding.loadingView, list)
      } else {
        binding.loadingView.visibility = View.GONE
      }
      if (dateMills != 0L) {
        val monthTitle = DateUtils.formatDateTime(activity, dateMills, DayViewFragment.MONTH_YEAR_FLAG).toString()
        binding.dateLabel.text = monthTitle
      }
      builder.setView(binding.root)
      builder.setOnDismissListener {
        job?.cancel()
      }
      builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
        dialog.dismiss()
      }
      mDialog = builder.create()
      mDialog?.show()
    }
  }

  private fun showMessage(string: String) {
    Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
  }

  protected fun loadEvents(listView: RecyclerView, emptyView: View, list: List<EventModel>) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateMills
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    this.job?.cancel()
    this.job = launchDefault {
      val res = ArrayList<EventModel>()
      for (item in list) {
        val mDay = item.day
        val mMonth = item.month
        val mYear = item.year
        val type = item.viewType
        if (type == EventModel.BIRTHDAY && mDay == day && mMonth == month) {
          res.add(item)
        } else {
          if (mDay == day && mMonth == month && mYear == year) {
            res.add(item)
          }
        }
      }
      Timber.d("Search events: found -> %d", res.size)
      val sorted = try {
        res.asSequence().sortedBy { it.getMillis() }.toList()
      } catch (e: IllegalArgumentException) {
        res
      }
      withUIContext { showList(listView, emptyView, sorted) }
    }
  }

  private fun showList(listView: RecyclerView, emptyView: View, res: List<EventModel>) {
    val adapter = CalendarEventsAdapter(currentStateHolder)
    adapter.setEventListener(object : ActionsListener<EventModel> {
      override fun onAction(view: View, position: Int, t: EventModel?, actions: ListActions) {
        if (t != null) {
          val model = t.model
          if (model is UiBirthdayList) {
            birthdayResolver.resolveAction(view, model, actions)
          } else if (model is UiReminderListData) {
            reminderResolver.resolveAction(view, model, actions)
          }
        }
      }
    })
    adapter.showMore = false
    adapter.setData(res)
    listView.adapter = adapter
    listView.visible()
    emptyView.gone()
  }

  protected fun addReminder() {
    if (isAdded) {
      withActivity {
        PinLoginActivity.openLogged(it, Intent(it, CreateReminderActivity::class.java)
          .putExtra(Constants.INTENT_DATE, dateMills))
      }
    }
  }

  protected fun addBirthday() {
    if (isAdded) {
      withActivity {
        PinLoginActivity.openLogged(it, Intent(it, AddBirthdayActivity::class.java)
          .putExtra(Constants.INTENT_DATE, dateMills))
      }
    }
  }
}
