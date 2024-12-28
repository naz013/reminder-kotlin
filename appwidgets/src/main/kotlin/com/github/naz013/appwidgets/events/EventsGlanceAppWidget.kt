package com.github.naz013.appwidgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
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
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.birthdays.UiBirthdayWidgetList
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
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
    provideContent {
      GlanceAppWidgetTheme {
        EventsContent(currentState())
      }
    }
  }

  @Composable
  private fun EventsContent(
    state: EventsAppWidgetState,
    modifier: GlanceModifier = GlanceModifier
  ) {
    Column(
      modifier = modifier.fillMaxSize()
    ) {
      Row(
        modifier = GlanceModifier.fillMaxWidth()
          .height(56.dp)
          .roundedBackground(state.headerBackgroundColor),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
          text = state.headerText,
          modifier = GlanceModifier.fillMaxWidth()
            .defaultWeight(),
          style = TextStyle(
            fontSize = 18.sp,
            color = ColorProvider(
              day = state.headerContrastColor,
              night = state.headerContrastColor
            )
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
              onClick = actionStartActivity<EventsWidgetConfigActivity>(
                actionParametersOf(widgetIdKey to state.widgetId)
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_settings),
          contentDescription = null,
          colorFilter = ColorFilter.tint(
            colorProvider = ColorProvider(
              day = state.headerContrastColor,
              night = state.headerContrastColor
            )
          )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Image(
          modifier = GlanceModifier
            .size(44.dp)
            .padding(8.dp)
            .cornerRadius(16.dp)
            .clickable(
              onClick = actionStartActivity<AppWidgetActionActivity>(
                actionParametersOf(directionKey to Direction.ADD_REMINDER)
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_add),
          contentDescription = null,
          colorFilter = ColorFilter.tint(
            colorProvider = ColorProvider(
              day = state.headerContrastColor,
              night = state.headerContrastColor
            )
          )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
      }
      Spacer(modifier = GlanceModifier.height(4.dp))
      LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
        items(state.items.size) { index: Int ->
          ListItem(
            data = state.items[index],
            itemBackgroundColor = state.itemBackgroundColor,
            itemContrastColor = state.itemContrastColor,
            itemTextSize = state.itemTextSize
          )
        }
      }
    }
  }

  @Composable
  private fun ListItem(
    data: DateSorted,
    itemBackgroundColor: Int,
    itemContrastColor: Color,
    itemTextSize: TextUnit
  ) {
    val colorProvider = ColorProvider(
      day = itemContrastColor,
      night = itemContrastColor
    )
    Column(modifier = GlanceModifier.fillMaxWidth()) {
      Spacer(modifier = GlanceModifier.height(4.dp))
      when (data) {
        is UiBirthdayWidgetList -> {
          BirthdayItem(
            data = data,
            itemBackgroundColor = itemBackgroundColor,
            itemContrastColor = colorProvider,
            itemTextSize = itemTextSize
          )
        }

        is UiReminderWidgetList -> {
          ReminderItem(
            data = data,
            itemBackgroundColor = itemBackgroundColor,
            itemContrastColor = colorProvider,
            itemTextSize = itemTextSize
          )
        }

        is UiReminderWidgetShopList -> {
          TaskListReminderItem(
            data = data,
            itemBackgroundColor = itemBackgroundColor,
            itemContrastColor = colorProvider,
            itemTextSize = itemTextSize
          )
        }
      }
    }
  }

  @Composable
  private fun BirthdayItem(
    data: UiBirthdayWidgetList,
    itemBackgroundColor: Int,
    itemContrastColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .roundedBackground(itemBackgroundColor)
        .clickable(
          onClick = actionStartActivity<AppWidgetActionActivity>(
            actionParametersOf(
              directionKey to Direction.BIRTHDAY_PREVIEW,
              dataKey to createData(data.uuId)
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
        colorFilter = ColorFilter.tint(
          colorProvider = itemContrastColor
        )
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.name,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = itemContrastColor
          ),
          maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
          text = data.ageFormattedAndBirthdayDate,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = itemContrastColor
          ),
          maxLines = 2
        )
      }
    }
  }

  @Composable
  private fun ReminderItem(
    data: UiReminderWidgetList,
    itemBackgroundColor: Int,
    itemContrastColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .roundedBackground(itemBackgroundColor)
        .clickable(
          onClick = actionStartActivity<AppWidgetActionActivity>(
            actionParametersOf(
              directionKey to Direction.REMINDER_PREVIEW,
              dataKey to createData(data.uuId)
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
        colorFilter = ColorFilter.tint(
          colorProvider = itemContrastColor
        )
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.text,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = itemContrastColor
          ),
          maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
          text = data.dateTime,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = itemContrastColor
          ),
          maxLines = 2
        )
      }
    }
  }

  @Composable
  private fun TaskListReminderItem(
    data: UiReminderWidgetShopList,
    itemBackgroundColor: Int,
    itemContrastColor: ColorProvider,
    itemTextSize: TextUnit
  ) {
    Row(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .roundedBackground(itemBackgroundColor)
        .clickable(
          onClick = actionStartActivity<AppWidgetActionActivity>(
            actionParametersOf(
              directionKey to Direction.REMINDER_PREVIEW,
              dataKey to createData(data.uuId)
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
        colorFilter = ColorFilter.tint(
          colorProvider = itemContrastColor
        )
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
          text = data.text,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = itemTextSize,
            color = itemContrastColor
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
              color = itemContrastColor
            ),
            maxLines = 2
          )
        }
        Spacer(modifier = GlanceModifier.height(4.dp))
        data.items.forEach {
          TaskItem(
            data = it,
            itemContrastColor = itemContrastColor,
            itemTextSize = itemTextSize
          )
        }
      }
    }
  }

  @Composable
  private fun TaskItem(
    data: UiShopListWidget,
    itemContrastColor: ColorProvider,
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
        colorFilter = ColorFilter.tint(
          colorProvider = itemContrastColor
        )
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Text(
        text = data.text,
        modifier = GlanceModifier.fillMaxWidth(),
        style = TextStyle(
          fontSize = itemTextSize,
          color = itemContrastColor
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

  @OptIn(ExperimentalGlancePreviewApi::class)
  @Preview
  @Composable
  private fun EventsContentPreview() {
    GlanceAppWidgetTheme {
      EventsContent(
        modifier = GlanceModifier.width(320.dp),
        state = EventsAppWidgetState(
          widgetId = 0,
          headerText = "27 December 2024",
          headerBackgroundColor = 6,
          headerContrastColor = Color.Black,
          itemBackgroundColor = 6,
          itemContrastColor = Color.Black,
          itemTextSize = 18.sp,
          items = emptyList()
        )
      )
    }
  }

  companion object {
    private const val TAG = "EventsGlanceAppWidget"
  }
}
