package com.github.naz013.appwidgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetList
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.appwidgets.compose.EmptyData
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.appwidgets.events.data.DateSorted
import com.github.naz013.appwidgets.events.data.EventsAppWidgetState
import com.github.naz013.appwidgets.events.data.UiReminderWidgetList
import com.github.naz013.appwidgets.events.data.UiReminderWidgetShopList
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class EventsGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val widgetIdKey = ActionParameters.Key<Int>(
    AppWidgetManager.EXTRA_APPWIDGET_ID
  )
  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )
  private val composeResourceProvider: (Context) -> ComposeResourceProvider = {
    ComposeResourceProvider(it)
  }

  override val stateDefinition: GlanceStateDefinition<EventsAppWidgetState>
    get() = object : GlanceStateDefinition<EventsAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<EventsAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return EventsAppWidgetStateDataStore(
          eventsAppWidgetViewModel = get<EventsAppWidgetViewModel> {
            parametersOf(EventsWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val configIntent = Intent(context, EventsWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    provideContent {
      GlanceAppWidgetTheme {
        EventsContent(
          context = context,
          state = currentState(),
          configIntent = configIntent
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val configIntent = Intent(context, EventsWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val previewState = get<EventsAppWidgetViewModel> {
      parametersOf(EventsWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        EventsContent(
          context = context,
          state = previewState,
          configIntent = configIntent
        )
      }
    }
  }

  private fun viewIntent(context: Context): Intent {
    return Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
  }

  @Composable
  private fun EventsContent(
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
    state: EventsAppWidgetState,
    configIntent: Intent
  ) {
    val emptyStateText = context.getString(R.string.schedule_you_have_done_everything_for_today)
    val widgetColors = composeResourceProvider(context).getColors(state.backgroundColor)
    Column(
      modifier = modifier
        .fillMaxSize()
        .roundedBackground(widgetColors.background)
        .systemWidgetShape()
    ) {
      Row(
        modifier = GlanceModifier
          .fillMaxWidth()
          .height(56.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        Spacer(modifier = GlanceModifier.width(16.dp))
        Text(
          text = state.headerText,
          modifier = GlanceModifier.fillMaxWidth()
            .defaultWeight(),
          style = TextStyle(
            fontSize = 18.sp,
            color = widgetColors.foreground
          ),
          maxLines = 1
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Image(
          modifier = GlanceModifier
            .size(44.dp)
            .padding(8.dp)
            .cornerRadius(16.dp)
            .clickable(
              onClick = actionStartActivity(
                intent = configIntent,
                parameters = actionParametersOf(widgetIdKey to state.widgetId)
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_settings),
          contentDescription = null,
          colorFilter = ColorFilter.tint(colorProvider = widgetColors.foreground)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Image(
          modifier = GlanceModifier
            .size(44.dp)
            .padding(8.dp)
            .cornerRadius(16.dp)
            .clickable(
              onClick = actionStartActivity(
                intent = viewIntent(context),
                parameters = actionParametersOf(
                  directionKey to Direction.ADD_REMINDER,
                  widgetTypeKey to Widget.EVENTS
                )
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_add),
          contentDescription = null,
          colorFilter = ColorFilter.tint(colorProvider = widgetColors.foreground)
        )
        Spacer(modifier = GlanceModifier.width(16.dp))
      }
      Spacer(
        modifier = GlanceModifier
          .fillMaxWidth()
          .height(1.dp)
          .background(widgetColors.foreground)
      )
      if (state.items.isEmpty()) {
        EmptyData(
          modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
          text = emptyStateText,
          color = widgetColors.foreground
        )
      } else {
        LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
          items(state.items.size) { index: Int ->
            ListItem(
              context = context,
              data = state.items[index],
              foregroundColor = widgetColors.foreground,
              itemTextSize = state.itemTextSize
            )
          }
        }
      }
    }
  }

  @Composable
  private fun ListItem(
    context: Context,
    data: DateSorted,
    foregroundColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Column(
      modifier = GlanceModifier.fillMaxWidth()
    ) {
      when (data) {
        is UiBirthdayWidgetList -> {
          BirthdayItem(
            context = context,
            data = data,
            foregroundColor = foregroundColor,
            itemTextSize = itemTextSize
          )
        }

        is UiReminderWidgetList -> {
          ReminderItem(
            context = context,
            data = data,
            foregroundColor = foregroundColor,
            itemTextSize = itemTextSize
          )
        }

        is UiReminderWidgetShopList -> {
          TaskListReminderItem(
            context = context,
            data = data,
            foregroundColor = foregroundColor,
            itemTextSize = itemTextSize
          )
        }
      }
      Spacer(
        modifier = GlanceModifier
          .fillMaxWidth()
          .height(1.dp)
          .background(foregroundColor)
      )
    }
  }

  @Composable
  private fun BirthdayItem(
    context: Context,
    data: UiBirthdayWidgetList,
    foregroundColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent(context),
            parameters = actionParametersOf(
              directionKey to Direction.BIRTHDAY_PREVIEW,
              dataKey to createData(data.uuId),
              widgetTypeKey to Widget.EVENTS
            )
          )
        ),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier
          .size(40.dp)
          .padding(8.dp),
        provider = ImageProvider(R.drawable.ic_fluent_food_cake),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorProvider = foregroundColor)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.name,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = foregroundColor
          ),
          maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
          text = data.ageFormattedAndBirthdayDate,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = foregroundColor
          ),
          maxLines = 2
        )
      }
    }
  }

  @Composable
  private fun ReminderItem(
    context: Context,
    data: UiReminderWidgetList,
    foregroundColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent(context),
            parameters = actionParametersOf(
              directionKey to Direction.REMINDER_PREVIEW,
              dataKey to createData(data.uuId),
              widgetTypeKey to Widget.EVENTS
            )
          )
        ),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier
          .size(40.dp)
          .padding(8.dp),
        provider = ImageProvider(R.drawable.ic_fluent_clock_alarm),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorProvider = foregroundColor)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.text,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = foregroundColor
          ),
          maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
          text = data.dateTime,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = foregroundColor
          ),
          maxLines = 2
        )
      }
    }
  }

  @Composable
  private fun TaskListReminderItem(
    context: Context,
    data: UiReminderWidgetShopList,
    foregroundColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent(context),
            parameters = actionParametersOf(
              directionKey to Direction.REMINDER_PREVIEW,
              dataKey to createData(data.uuId),
              widgetTypeKey to Widget.EVENTS
            )
          )
        ),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier
          .size(40.dp)
          .padding(8.dp),
        provider = ImageProvider(R.drawable.ic_fluent_cart),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorProvider = foregroundColor)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.text,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = foregroundColor
          ),
          maxLines = 2
        )
        if (data.dateTime != null) {
          Spacer(modifier = GlanceModifier.height(4.dp))
          Text(
            text = data.dateTime,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
              fontSize = itemTextSize,
              color = foregroundColor
            ),
            maxLines = 2
          )
        }
        Spacer(modifier = GlanceModifier.height(4.dp))
        data.items.forEach {
          TaskItem(
            data = it,
            foregroundColor = foregroundColor,
            itemTextSize = itemTextSize
          )
        }
      }
    }
  }

  @Composable
  private fun TaskItem(
    data: UiShopListWidget,
    foregroundColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth(),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier
          .size(32.dp)
          .padding(4.dp),
        provider = ImageProvider(data.iconRes),
        contentDescription = null,
        colorFilter = ColorFilter.tint(colorProvider = foregroundColor)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Text(
        text = data.text,
        modifier = GlanceModifier.fillMaxWidth(),
        style = TextStyle(
          fontSize = itemTextSize,
          color = foregroundColor
        ),
        maxLines = 1
      )
    }
  }

  private fun createData(id: String): WidgetIntentProtocol {
    return WidgetIntentProtocol(
      mapOf<String, Any?>(
        Pair(IntentKeys.INTENT_ID, id)
      )
    )
  }

  companion object {
    private const val TAG = "EventsGlanceAppWidget"
  }
}
