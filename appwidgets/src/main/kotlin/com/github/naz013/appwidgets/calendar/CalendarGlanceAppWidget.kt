package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.calendar.data.CalendarAppWidgetState
import com.github.naz013.appwidgets.calendar.data.UiCalendarDay
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.paletteContrastColor
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.navigation.DeepLinkDestination
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.io.File

internal class CalendarGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val widgetIdKey = ActionParameters.Key<Int>(
    AppWidgetManager.EXTRA_APPWIDGET_ID
  )
  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<CalendarAppWidgetState>
    get() = object : GlanceStateDefinition<CalendarAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<CalendarAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        val prefsProvider = CalendarWidgetPrefsProvider(context, widgetId)
        return CalendarAppWidgetStateDataStore(
          calendarAppWidgetViewModel = get<CalendarAppWidgetViewModel> {
            parametersOf(context, prefsProvider)
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val configIntent = Intent(context, CalendarWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val addReminderIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val dateTimeManager = get<DateTimeManager>()
    provideContent {
      GlanceAppWidgetTheme {
        CalendarContent(
          context = context,
          dateTimeManager = dateTimeManager,
          state = currentState(),
          configIntent = configIntent,
          addReminderIntent = addReminderIntent
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val configIntent = Intent(context, CalendarWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val addReminderIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val dateTimeManager = get<DateTimeManager>()
    val previewState = get<CalendarAppWidgetViewModel> {
      parametersOf(context, CalendarWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        CalendarContent(
          context = context,
          dateTimeManager = dateTimeManager,
          state = previewState,
          configIntent = configIntent,
          addReminderIntent = addReminderIntent
        )
      }
    }
  }

  @Composable
  private fun CalendarContent(
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
    dateTimeManager: DateTimeManager,
    state: CalendarAppWidgetState,
    configIntent: Intent,
    addReminderIntent: Intent
  ) {
    val headerContrastColorProvider = paletteContrastColor(
      state.headerBackgroundColor,
      state.headerContrastColor
    )
    Column(modifier = modifier.fillMaxSize().systemWidgetShape()) {
      Row(
        modifier = GlanceModifier.fillMaxWidth()
          .height(50.dp)
          .roundedBackground(state.headerBackgroundColor),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        HeaderIcon(
          iconRes = R.drawable.ic_fluent_chevron_left,
          tintColor = headerContrastColorProvider,
          onClick = actionRunCallback<CalendarPreviousMonthActionCallback>()
        )
        Text(
          text = state.monthYearText,
          modifier = GlanceModifier.fillMaxWidth()
            .defaultWeight(),
          style = TextStyle(
            fontSize = 16.sp,
            color = headerContrastColorProvider
          ),
          maxLines = 1
        )
        HeaderIcon(
          iconRes = R.drawable.ic_fluent_chevron_right,
          tintColor = headerContrastColorProvider,
          onClick = actionRunCallback<CalendarNextMonthActionCallback>()
        )
        HeaderIcon(
          iconRes = R.drawable.ic_fluent_settings,
          tintColor = headerContrastColorProvider,
          onClick = actionStartActivity(
            intent = configIntent,
            parameters = actionParametersOf(widgetIdKey to state.widgetId)
          )
        )
        HeaderIcon(
          iconRes = R.drawable.ic_fluent_add,
          tintColor = headerContrastColorProvider,
          onClick = actionStartActivity(
            intent = addReminderIntent,
            parameters = actionParametersOf(
              directionKey to Direction.ADD_REMINDER,
              widgetTypeKey to Widget.CALENDAR
            )
          )
        )
      }
      Spacer(modifier = GlanceModifier.height(4.dp))
      Column(
        modifier = GlanceModifier.fillMaxWidth()
          .fillMaxHeight()
          .roundedBackground(state.backgroundColor)
      ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
          state.weekdays.forEach { weekday ->
            Text(
              text = weekday,
              modifier = GlanceModifier.defaultWeight().padding(4.dp),
              style = TextStyle(
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = paletteContrastColor(state.backgroundColor, state.backgroundContrastColor)
              ),
              maxLines = 1
            )
          }
        }
        state.days.chunked(7).forEach { week ->
          Row(
            modifier = GlanceModifier.fillMaxWidth()
              .defaultWeight()
          ) {
            week.forEach { day ->
              DayCell(
                context = context,
                dateTimeManager = dateTimeManager,
                day = day,
                backgroundColorIndex = state.backgroundColor,
                currentMonthTextColor = state.backgroundContrastColor,
                todayMarkColor = state.todayMarkColor,
                reminderMarkColor = state.reminderMarkColor,
                birthdayMarkColor = state.birthdayMarkColor
              )
            }
          }
        }
      }
    }
  }

  @Composable
  private fun RowScope.HeaderIcon(
    iconRes: Int,
    tintColor: ColorProvider,
    onClick: Action
  ) {
    Image(
      modifier = GlanceModifier
        .size(40.dp)
        .padding(8.dp)
        .cornerRadius(16.dp)
        .clickable(onClick = onClick),
      provider = ImageProvider(iconRes),
      contentDescription = null,
      colorFilter = ColorFilter.tint(tintColor)
    )
  }

  @Composable
  private fun RowScope.DayCell(
    context: Context,
    dateTimeManager: DateTimeManager,
    day: UiCalendarDay,
    backgroundColorIndex: Int,
    currentMonthTextColor: Color,
    todayMarkColor: Color,
    reminderMarkColor: Color,
    birthdayMarkColor: Color
  ) {
    val dayMillis = dateTimeManager.toMillis(LocalDateTime.of(day.date, LocalTime.now()))
    val dayIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      putExtra(AppWidgetActionActivity.DIRECTION, Direction.HOME)
      putExtra(AppWidgetActionActivity.WIDGET_TYPE, Widget.CALENDAR)
      putExtra(
        DeepLinkDestination.KEY,
        DayViewScreen(dayMillis)
      )
    }
    val outOfMonthColor = Color(0xFF303030)
    val textColorProvider = if (day.isCurrentMonth) {
      paletteContrastColor(backgroundColorIndex, currentMonthTextColor)
    } else {
      ColorProvider(day = outOfMonthColor, night = outOfMonthColor)
    }
    Box(
      modifier = GlanceModifier
        .defaultWeight()
        .fillMaxHeight()
        .clickable(onClick = actionStartActivity(intent = dayIntent)),
      contentAlignment = Alignment.Center
    ) {
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        MarkBar(visible = day.isToday, color = todayMarkColor)
        MarkBar(visible = day.hasReminder, color = reminderMarkColor)
        MarkBar(visible = day.hasBirthday, color = birthdayMarkColor)
      }
      Text(
        text = day.dayText,
        style = TextStyle(
          fontSize = 13.sp,
          textAlign = TextAlign.Center,
          color = textColorProvider
        ),
        maxLines = 1
      )
    }
  }

  @Composable
  private fun MarkBar(visible: Boolean, color: Color) {
    if (visible) {
      Spacer(
        modifier = GlanceModifier.fillMaxWidth()
          .height(3.dp)
          .background(color)
      )
    } else {
      Spacer(modifier = GlanceModifier.fillMaxWidth().height(3.dp))
    }
  }

  companion object {
    private const val TAG = "CalendarGlanceAppWidget"
  }
}
