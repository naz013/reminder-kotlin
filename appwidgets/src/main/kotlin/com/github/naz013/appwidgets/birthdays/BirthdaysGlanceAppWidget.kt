package com.github.naz013.appwidgets.birthdays

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.appwidgets.compose.EmptyData
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class BirthdaysGlanceAppWidget : GlanceAppWidget(), KoinComponent {

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

  override val stateDefinition: GlanceStateDefinition<BirthdaysAppWidgetState>
    get() = object : GlanceStateDefinition<BirthdaysAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<BirthdaysAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return BirthdaysAppWidgetStateDataStore(
          birthdaysAppWidgetViewModel = get<BirthdaysAppWidgetViewModel> {
            parametersOf(BirthdaysWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val configIntent = Intent(context, BirthdaysWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val titleText = context.getString(R.string.birthdays)
    val emptyStateText = context.getString(R.string.no_upcoming_birthdays)
    provideContent {
      GlanceAppWidgetTheme {
        BirthdaysContent(
          context = context,
          state = currentState(),
          configIntent = configIntent,
          titleText = titleText,
          emptyStateText = emptyStateText
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val configIntent = Intent(context, BirthdaysWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val titleText = context.getString(R.string.birthdays)
    val emptyStateText = context.getString(R.string.no_upcoming_birthdays)
    val previewState = get<BirthdaysAppWidgetViewModel> {
      parametersOf(BirthdaysWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        BirthdaysContent(
          context = context,
          state = previewState,
          configIntent = configIntent,
          titleText = titleText,
          emptyStateText = emptyStateText
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
  private fun BirthdaysContent(
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
    state: BirthdaysAppWidgetState,
    configIntent: Intent,
    titleText: String,
    emptyStateText: String
  ) {
    val widgetColors = composeResourceProvider(context).getColors(state.backgroundColor)
    Column(
      modifier = modifier.fillMaxSize()
        .roundedBackground(widgetColors.background)
        .systemWidgetShape()
    ) {
      Row(
        modifier = GlanceModifier.fillMaxWidth()
          .height(56.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        Spacer(modifier = GlanceModifier.width(16.dp))
        Text(
          text = titleText,
          modifier = GlanceModifier.fillMaxWidth()
            .defaultWeight(),
          style = TextStyle(
            fontSize = 20.sp,
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
          colorFilter = ColorFilter.tint(widgetColors.foreground)
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
                  directionKey to Direction.ADD_BIRTHDAY,
                  widgetTypeKey to Widget.BIRTHDAYS
                )
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_add),
          contentDescription = null,
          colorFilter = ColorFilter.tint(widgetColors.foreground)
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
        LazyColumn(
          modifier = GlanceModifier.fillMaxWidth(),
        ) {
          items(state.items.size) { index: Int ->
            BirthdayItem(
              context = context,
              data = state.items[index],
              textColor = widgetColors.foreground,
            )
          }
        }
      }
    }
  }

  @Composable
  private fun BirthdayItem(
    context: Context,
    data: UiBirthdayWidgetList,
    textColor: ColorProvider,
  ) {
    Column(
      modifier = GlanceModifier.fillMaxWidth(),
    ) {
      Column(
        modifier = GlanceModifier.fillMaxWidth()
          .padding(vertical = 8.dp, horizontal = 16.dp)
          .clickable(
            onClick = actionStartActivity(
              intent = viewIntent(context),
              parameters = actionParametersOf(
                directionKey to Direction.BIRTHDAY_PREVIEW,
                dataKey to WidgetIntentProtocol(
                  mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, data.uuId))
                ),
                widgetTypeKey to Widget.BIRTHDAYS
              )
            )
          )
      ) {
        Text(
          text = data.name,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = 14.sp,
            color = textColor
          ),
          maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
          text = data.ageFormattedAndBirthdayDate,
          modifier = GlanceModifier.fillMaxWidth(),
          style = TextStyle(
            fontSize = 14.sp,
            color = textColor
          ),
          maxLines = 2
        )
      }
      Spacer(
        modifier = GlanceModifier
          .fillMaxWidth()
          .height(1.dp)
          .background(textColor)
      )
    }
  }

  companion object {
    private const val TAG = "BirthdaysGlanceAppWidget"
  }
}
