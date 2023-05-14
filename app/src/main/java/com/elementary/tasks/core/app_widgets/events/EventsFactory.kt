package com.elementary.tasks.core.app_widgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.AppWidgetActionActivity
import com.elementary.tasks.core.app_widgets.Direction
import com.elementary.tasks.core.app_widgets.WidgetIntentProtocol
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.data.adapter.birthday.UiBirthdayWidgetListAdapter
import com.elementary.tasks.core.data.adapter.reminder.UiReminderWidgetListAdapter
import com.elementary.tasks.core.data.repository.BirthdayRepository
import com.elementary.tasks.core.data.repository.ReminderRepository
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayWidgetList
import com.elementary.tasks.core.data.ui.reminder.widget.UiReminderWidgetList
import com.elementary.tasks.core.data.ui.reminder.widget.UiReminderWidgetShopList
import com.elementary.tasks.core.data.ui.widget.DateSorted
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.font.FontParams

class EventsFactory(
  private val context: Context,
  intent: Intent,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val reminderRepository: ReminderRepository,
  private val birthdayRepository: BirthdayRepository,
  private val uiReminderWidgetListAdapter: UiReminderWidgetListAdapter,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter
) : RemoteViewsService.RemoteViewsFactory {

  private var data = mutableListOf<DateSorted>()
  private var itemTextSize: Float = FontParams.DEFAULT_FONT_SIZE.toFloat()
  private var itemBg: Int = 0
  @ColorInt
  private var textColor: Int = Color.BLACK

  private val widgetID: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private val prefsProvider = EventsWidgetPrefsProvider(context, widgetID)

  override fun onCreate() {
    data.clear()
  }

  override fun onDataSetChanged() {
    data.clear()

    itemTextSize = prefsProvider.getTextSize()
    val itemBgColor = prefsProvider.getItemBackground()

    itemBg = WidgetUtils.newWidgetBg(itemBgColor)

    textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }

    reminderRepository.getActive()
      .map { uiReminderWidgetListAdapter.create(it, textColor) }
      .also { data.addAll(it) }

    if (prefs.isBirthdayInWidgetEnabled) {
      val dateTime = dateTimeManager.getCurrentDateTime()

      birthdayRepository.getByDayMonth(dateTime.dayOfMonth, dateTime.monthValue - 1)
        .map { uiBirthdayWidgetListAdapter.convert(it) }
        .also { data.addAll(it) }
    }

    data = data.sortedWith(compareBy { it.millis }).toMutableList()
  }

  override fun onDestroy() {
    data.clear()
  }

  override fun getCount(): Int {
    return data.size
  }

  override fun getViewAt(i: Int): RemoteViews? {
    var rv: RemoteViews? = null
    if (i >= count) {
      return null
    }

    when (val item = data[i]) {
      is UiBirthdayWidgetList -> {
        val icon = ViewUtils.createIcon(context, R.drawable.ic_twotone_cake_24px, textColor)

        rv = RemoteViews(context.packageName, R.layout.list_item_widget_birthday)

        rv.setInt(R.id.itemBackgroundView, "setBackgroundResource", itemBg)

        rv.setTextColor(R.id.nameView, textColor)
        rv.setTextColor(R.id.ageBirthDateView, textColor)
        rv.setTextColor(R.id.leftTimeView, textColor)

        rv.setImageViewBitmap(R.id.statusIconView, icon)

        rv.setTextViewText(R.id.nameView, item.name)
        rv.setTextViewText(R.id.ageBirthDateView, item.ageFormattedAndBirthdayDate)
        rv.setTextViewText(R.id.leftTimeView, item.remainingTimeFormatted)

        val data = WidgetIntentProtocol(
          mapOf<String, Any?>(
            Pair(Constants.INTENT_ID, item.uuId)
          )
        )

        val fillInIntent = Intent()
        fillInIntent.putExtra(AppWidgetActionActivity.DATA, data)
        fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.BIRTHDAY)
        rv.setOnClickFillInIntent(R.id.itemBackgroundView, fillInIntent)
      }
      is UiReminderWidgetShopList -> {
        val icon = ViewUtils.createIcon(context, R.drawable.ic_twotone_shopping_cart_24px, textColor)

        rv = RemoteViews(context.packageName, R.layout.list_item_widget_shop_list)
        rv.setInt(R.id.itemBackgroundView, "setBackgroundResource", itemBg)

        rv.setImageViewBitmap(R.id.statusIconView, icon)

        rv.setTextViewText(R.id.taskTextView, item.text)
        rv.setTextColor(R.id.taskTextView, textColor)
        rv.setTextViewTextSize(R.id.taskTextView, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

        if (!item.dateTime.isNullOrEmpty()) {
          rv.setTextViewText(R.id.dateTimeView, item.dateTime)
          rv.setViewVisibility(R.id.dateTimeView, View.VISIBLE)
        } else {
          rv.setViewVisibility(R.id.dateTimeView, View.GONE)
        }

        var count = 0

        rv.removeAllViews(R.id.todoList)

        for (task in item.items) {
          val view = RemoteViews(context.packageName, R.layout.list_item_widget_shop_item)

          view.setImageViewBitmap(R.id.checkView, task.icon)
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
            view.setTextViewText(R.id.shopText, task.text)
            rv.addView(R.id.todoList, view)
          }
        }

        val data = WidgetIntentProtocol(
          mapOf<String, Any?>(
            Pair(Constants.INTENT_ID, item.uuId)
          )
        )

        val fillInIntent = Intent()
        fillInIntent.putExtra(AppWidgetActionActivity.DATA, data)
        fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.REMINDER)
        rv.setOnClickFillInIntent(R.id.taskTextView, fillInIntent)
        rv.setOnClickFillInIntent(R.id.itemBackgroundView, fillInIntent)
        rv.setOnClickFillInIntent(R.id.todoList, fillInIntent)
      }
      is UiReminderWidgetList -> {
        val icon = ViewUtils.createIcon(context, R.drawable.ic_twotone_alarm_24px, textColor)

        rv = RemoteViews(context.packageName, R.layout.list_item_widget_events)

        rv.setInt(R.id.itemBackgroundView, "setBackgroundResource", itemBg)

        rv.setTextColor(R.id.taskTextView, textColor)
        rv.setTextColor(R.id.dateTimeView, textColor)
        rv.setTextColor(R.id.leftTimeView, textColor)

        rv.setTextViewTextSize(R.id.taskTextView, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
        rv.setTextViewTextSize(R.id.dateTimeView, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
        rv.setTextViewTextSize(R.id.leftTimeView, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

        rv.setImageViewBitmap(R.id.statusIconView, icon)
        rv.setTextViewText(R.id.taskTextView, item.text)
        rv.setTextViewText(R.id.dateTimeView, item.dateTime)
        rv.setTextViewText(R.id.leftTimeView, item.remainingTimeFormatted)

        val data = WidgetIntentProtocol(
          mapOf<String, Any?>(
            Pair(Constants.INTENT_ID, item.uuId)
          )
        )

        val fillInIntent = Intent()
        fillInIntent.putExtra(AppWidgetActionActivity.DATA, data)
        fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.REMINDER)
        rv.setOnClickFillInIntent(R.id.taskTextView, fillInIntent)
        rv.setOnClickFillInIntent(R.id.itemBackgroundView, fillInIntent)
      }
    }
    return rv
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getViewTypeCount(): Int {
    return 3
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun hasStableIds(): Boolean {
    return true
  }
}
