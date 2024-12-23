package com.github.naz013.appwidgets.birthdays

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.coroutine.invokeSuspend
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase

internal class BirthdaysFactory(
  private val context: Context,
  intent: Intent,
  private val uiBirthdayWidgetListAdapter: UiBirthdayWidgetListAdapter,
  private val getAllBirthdaysUseCase: GetAllBirthdaysUseCase
) : RemoteViewsService.RemoteViewsFactory {

  private val widgetID: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private val prefsProvider = BirthdaysWidgetPrefsProvider(context, widgetID)

  private val list = mutableListOf<UiBirthdayWidgetList>()
  private var icon: Bitmap? = null
  private var itemBg: Int = 0

  @ColorInt
  private var textColor: Int = Color.BLACK

  override fun onCreate() {
    list.clear()
  }

  override fun onDataSetChanged() {
    val itemBgColor = prefsProvider.getItemBackground()
    itemBg = WidgetUtils.newWidgetBg(itemBgColor)

    textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }

    icon = ViewUtils.createIcon(context, R.drawable.ic_fluent_food_cake, textColor)

    list.clear()

    invokeSuspend { getAllBirthdaysUseCase() }
      .map { uiBirthdayWidgetListAdapter.convert(it) }
      .sortedBy { it.millis }
      .also { list.addAll(it) }
  }

  override fun onDestroy() {
    list.clear()
  }

  override fun getCount(): Int {
    return list.size
  }

  override fun getViewAt(i: Int): RemoteViews {
    val rv = RemoteViews(context.packageName, R.layout.list_item_widget_birthday)

    rv.setTextViewText(R.id.nameView, "")
    rv.setTextViewText(R.id.ageBirthDateView, "")
    rv.setTextViewText(R.id.leftTimeView, "")

    if (i >= count) {
      rv.setTextViewText(R.id.nameView, context.getString(R.string.failed_to_load))
      return rv
    }

    val birthday = list[i]

    rv.setInt(R.id.itemBackgroundView, "setBackgroundResource", itemBg)

    rv.setTextColor(R.id.nameView, textColor)
    rv.setTextColor(R.id.ageBirthDateView, textColor)
    rv.setTextColor(R.id.leftTimeView, textColor)

    rv.setImageViewBitmap(R.id.statusIconView, icon)

    rv.setTextViewText(R.id.nameView, birthday.name)
    rv.setTextViewText(R.id.ageBirthDateView, birthday.ageFormattedAndBirthdayDate)

    if (birthday.remainingTimeFormatted == null) {
      rv.setViewVisibility(R.id.leftTimeView, View.GONE)
    } else {
      rv.setTextViewText(R.id.leftTimeView, birthday.remainingTimeFormatted)
      rv.setViewVisibility(R.id.leftTimeView, View.VISIBLE)
    }

    val data = WidgetIntentProtocol(
      mapOf<String, Any?>(
        Pair(IntentKeys.INTENT_ID, birthday.uuId)
      )
    )

    val fillInIntent = Intent()
    fillInIntent.putExtra(AppWidgetActionActivity.DATA, data)
    fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.BIRTHDAY_PREVIEW)
    rv.setOnClickFillInIntent(R.id.itemBackgroundView, fillInIntent)
    return rv
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getViewTypeCount(): Int {
    return 1
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun hasStableIds(): Boolean {
    return true
  }
}
