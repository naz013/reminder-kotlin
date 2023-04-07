package com.elementary.tasks.navigation.fragments

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.data.ui.UiReminderListData
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.day_view.day.CalendarEventsAdapter
import com.elementary.tasks.day_view.day.EventModel
import com.elementary.tasks.monthview.DayBottomSheetDialog
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.ReminderResolver
import com.elementary.tasks.reminder.create.CreateReminderActivity
import kotlinx.coroutines.Job
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import timber.log.Timber

abstract class BaseCalendarFragment<B : ViewBinding> : BaseNavigationFragment<B>() {

  protected val dateTimeManager by inject<DateTimeManager>()

  protected var date: LocalDate = LocalDate.now()
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

  protected fun showActionDialog(list: List<EventModel>) {
    withContext {
      DayBottomSheetDialog(
        context = it,
        label = dateTimeManager.formatCalendarDate(date),
        list = list,
        addReminderCallback = { addReminder() },
        addBirthdayCallback = { addBirthday() },
        loadCallback = { listView, loadingView, emptyView, list ->
          loadEvents(listView, loadingView, emptyView, list)
        }
      ).show()
    }
  }

  protected fun loadEvents(
    listView: RecyclerView,
    loadingView: View,
    emptyView: View,
    list: List<EventModel>
  ) {
    this.job?.cancel()
    this.job = launchDefault {
      val res = ArrayList<EventModel>()
      for (item in list) {
        if (item.viewType == EventModel.BIRTHDAY && item.day == date.dayOfMonth &&
          item.monthValue == date.monthValue
        ) {
          res.add(item)
        } else if (item.day == date.dayOfMonth && item.monthValue == date.monthValue &&
          item.year == date.year
        ) {
          res.add(item)
        }
      }
      Timber.d("Search events: found -> ${res.size}")
      val sorted = try {
        res.asSequence().sortedBy { it.getMillis() }.toList()
      } catch (e: IllegalArgumentException) {
        res
      }
      withUIContext { showList(listView, loadingView, emptyView, sorted) }
    }
  }

  private fun showList(
    listView: RecyclerView,
    loadingView: View,
    emptyView: View,
    res: List<EventModel>
  ) {
    val adapter = CalendarEventsAdapter(isDark)
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
    listView.visibleGone(res.isNotEmpty())
    emptyView.visibleGone(res.isEmpty())
    loadingView.gone()
  }

  protected fun addReminder() {
    if (isAdded) {
      withActivity {
        PinLoginActivity.openLogged(
          it, Intent(it, CreateReminderActivity::class.java)
            .putExtra(Constants.INTENT_DATE, date)
        )
      }
    }
  }

  protected fun addBirthday() {
    if (isAdded) {
      withActivity {
        PinLoginActivity.openLogged(
          it, Intent(it, AddBirthdayActivity::class.java)
            .putExtra(Constants.INTENT_DATE, date)
        )
      }
    }
  }
}
