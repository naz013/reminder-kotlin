package com.elementary.tasks.core.app_widgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.Actions
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.ViewUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Locale

class EventsFactory(
  private val context: Context,
  intent: Intent,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val contactsReader: ContactsReader,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository
) : RemoteViewsService.RemoteViewsFactory {

  private var data = mutableListOf<CalendarItem>()
  private val map = mutableMapOf<String, Reminder>()
  private val widgetID: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )

  override fun onCreate() {
    data.clear()
    map.clear()
  }

  override fun onDataSetChanged() {
    data.clear()
    map.clear()

    val reminders = reminderRepository.getActive()

    for (reminder in reminders) {
      val type = reminder.type
      val summary = reminder.summary
      val eventTime = dateTimeManager.fromGmtToLocal(reminder.eventTime)
      val id = reminder.uuId

      var time = ""
      var date = ""
      var viewType = 1
      when {
        Reminder.isGpsType(type) -> {
          val place = reminder.places[0]
          date = String.format(Locale.getDefault(), "%.5f", place.latitude)
          time = String.format(Locale.getDefault(), "%.5f", place.longitude)
        }
        Reminder.isBase(type, Reminder.BY_WEEK) && eventTime != null -> {
          date = dateTimeManager.getRepeatString(reminder.weekdays)
          time = dateTimeManager.getTime(eventTime.toLocalTime())
        }
        Reminder.isBase(type, Reminder.BY_MONTH) && eventTime != null -> {
          date = eventTime.toLocalDate().format(dateTimeManager.dateFormatter())
          time = dateTimeManager.getTime(eventTime.toLocalTime())
        }
        Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> {
          if (reminder.hasReminder && eventTime != null) {
            val dT = dateTimeManager.getNextDateTime(eventTime)
            date = dT[0]
            time = dT[1]
          }
          viewType = 2
          map[id] = reminder
        }
        else -> {
          val dT = dateTimeManager.getNextDateTime(eventTime)
          date = dT[0]
          time = dT[1]
        }
      }
      data.add(
        CalendarItem(
          type = CalendarItem.Type.REMINDER,
          summary = summary,
          number = reminder.target,
          id = id,
          timeFormatted = time,
          dateFormatted = date,
          dateTime = eventTime,
          viewType = viewType,
          item = reminder
        )
      )
    }

    if (prefs.isBirthdayInWidgetEnabled) {
      var n = 0
      val birthTime = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now()
      var dateTime = LocalDateTime.now()
      do {
        val list = birthdayRepository.getByDayMonth(dateTime.dayOfMonth, dateTime.monthValue - 1)
        for (item in list) {
          val birthday = item.date
          val name = item.name
          data.add(
            CalendarItem(
              type = CalendarItem.Type.BIRTHDAY,
              summary = context.getString(R.string.birthday),
              number = name,
              timeFormatted = "",
              dateFormatted = birthday,
              id = item.key,
              dateTime = dateTimeManager.getFutureBirthdayDate(birthTime, item.date).dateTime,
              viewType = 1,
              item = item
            )
          )
        }
        dateTime = dateTime.plusDays(1)
        n++
      } while (n <= 7)
    }
    data = data.sortedBy { it.dateTime }.toMutableList()
  }

  override fun onDestroy() {
    map.clear()
    data.clear()
  }

  override fun getCount(): Int {
    return data.size
  }

  override fun getViewAt(i: Int): RemoteViews? {
    val sp = context.getSharedPreferences(EventsWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
    val itemTextSize = sp.getFloat(EventsWidgetConfigActivity.WIDGET_TEXT_SIZE + widgetID, 0f)
    val itemBgColor = sp.getInt(EventsWidgetConfigActivity.WIDGET_ITEM_BG + widgetID, 0)

    val textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }

    var rv: RemoteViews? = null
    if (i >= count) {
      return null
    }
    val item = data[i]
    if (item.viewType == 1) {
      rv = RemoteViews(context.packageName, R.layout.list_item_widget_events)

      rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

      rv.setTextColor(R.id.taskText, textColor)
      rv.setTextColor(R.id.taskDate, textColor)
      rv.setTextColor(R.id.taskNumber, textColor)
      rv.setTextColor(R.id.taskTime, textColor)
      rv.setTextColor(R.id.leftTime, textColor)

      val icon = if (item.type == CalendarItem.Type.REMINDER) {
        ViewUtils.createIcon(context, R.drawable.ic_twotone_alarm_24px, textColor)
      } else {
        ViewUtils.createIcon(context, R.drawable.ic_twotone_cake_24px, textColor)
      }
      rv.setImageViewBitmap(R.id.statusIcon, icon)

      var task = item.summary
      if (task.isNullOrBlank()) {
        task = contactsReader.getNameFromNumber(item.number)
      }
      rv.setTextViewText(R.id.taskText, task)

      rv.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
      rv.setTextViewTextSize(R.id.taskNumber, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
      rv.setTextViewTextSize(R.id.taskDate, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
      rv.setTextViewTextSize(R.id.taskTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
      rv.setTextViewTextSize(R.id.leftTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

      val number = item.number
      if (!number.isNullOrBlank()) {
        rv.setTextViewText(R.id.taskNumber, number)
        rv.setViewVisibility(R.id.taskNumber, View.VISIBLE)
      } else {
        rv.setViewVisibility(R.id.taskNumber, View.GONE)
      }
      rv.setTextViewText(R.id.taskDate, item.dateFormatted)
      rv.setTextViewText(R.id.taskTime, item.timeFormatted)
      rv.setTextViewText(R.id.leftTime, dateTimeManager.getRemaining(item.dateTime))

      if (item.id != null) {
        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.INTENT_ID, item.id)
        fillInIntent.action = Actions.Reminder.ACTION_EDIT_EVENT
        if (item.type == CalendarItem.Type.REMINDER) {
          fillInIntent.putExtra(EventActionReceiver.TYPE, true)
        } else {
          fillInIntent.putExtra(EventActionReceiver.TYPE, false)
        }
        rv.setOnClickFillInIntent(R.id.taskDate, fillInIntent)
        rv.setOnClickFillInIntent(R.id.taskTime, fillInIntent)
        rv.setOnClickFillInIntent(R.id.taskNumber, fillInIntent)
        rv.setOnClickFillInIntent(R.id.taskText, fillInIntent)
        rv.setOnClickFillInIntent(R.id.listItemCard, fillInIntent)
      }
    }
    if (item.viewType == 2) {
      rv = RemoteViews(context.packageName, R.layout.list_item_widget_shop_list)
      rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

      rv.setImageViewBitmap(R.id.statusIcon, ViewUtils.createIcon(context, R.drawable.ic_twotone_shopping_cart_24px, textColor))

      val task = item.summary
      rv.setTextViewText(R.id.taskText, task)
      rv.setTextColor(R.id.taskText, textColor)
      rv.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

      val reminder = map[item.id]
      val lists = reminder?.shoppings ?: listOf()

      if (reminder != null && reminder.hasReminder) {
        rv.setTextViewText(R.id.taskDate, item.dateFormatted)
        rv.setTextViewText(R.id.taskTime, item.timeFormatted)
        rv.setViewVisibility(R.id.dateTimeView, View.VISIBLE)
      } else {
        rv.setViewVisibility(R.id.dateTimeView, View.GONE)
      }

      var count = 0

      rv.removeAllViews(R.id.todoList)

      val checkedIcon = ViewUtils.createIcon(context, R.drawable.ic_twotone_check_box_24px, textColor)
      val unCheckedIcon = ViewUtils.createIcon(context, R.drawable.ic_twotone_check_box_outline_blank_24px, textColor)

      for (list in lists) {
        val view = RemoteViews(context.packageName, R.layout.list_item_widget_shop_item)
        val icon = if (list.isChecked) checkedIcon else unCheckedIcon
        view.setImageViewBitmap(R.id.checkView, icon)
        view.setTextColor(R.id.shopText, textColor)
        view.setTextViewTextSize(R.id.shopText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
        count++
        if (count == 9) {
          view.setViewVisibility(R.id.checkView, View.INVISIBLE)
          view.setTextViewText(R.id.shopText, "...")
          rv.addView(R.id.todoList, view)
          break
        } else {
          view.setViewVisibility(R.id.checkView, View.VISIBLE)
          view.setTextViewText(R.id.shopText, list.summary)
          rv.addView(R.id.todoList, view)
        }
      }

      val fillInIntent = Intent()
      fillInIntent.putExtra(Constants.INTENT_ID, item.id)
      fillInIntent.putExtra(EventActionReceiver.TYPE, true)
      rv.setOnClickFillInIntent(R.id.taskText, fillInIntent)
      rv.setOnClickFillInIntent(R.id.listItemCard, fillInIntent)
      rv.setOnClickFillInIntent(R.id.todoList, fillInIntent)
    }
    return rv
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getViewTypeCount(): Int {
    return 2
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun hasStableIds(): Boolean {
    return true
  }
}