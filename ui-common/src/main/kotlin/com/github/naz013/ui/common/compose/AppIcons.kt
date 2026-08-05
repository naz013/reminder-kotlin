package com.github.naz013.ui.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.github.naz013.ui.common.R

object AppIcons {

  object Fluent {
    val Checkmark: Painter @Composable get() = painterResource(R.drawable.ic_fluent_checkmark)
    val Dismiss: Painter @Composable get() = painterResource(R.drawable.ic_fluent_dismiss)
    val Text: Painter @Composable get() = painterResource(R.drawable.ic_fluent_text)
    val Cloud: Painter @Composable get() = painterResource(R.drawable.ic_fluent_cloud)
    val Star: Painter @Composable get() = painterResource(R.drawable.ic_fluent_star)
    val ArrowRepeatAll: Painter @Composable get() = painterResource(R.drawable.ic_fluent_arrow_repeat_all)
    val Person: Painter @Composable get() = painterResource(R.drawable.ic_fluent_person)
    val Settings: Painter @Composable get() = painterResource(R.drawable.ic_fluent_settings)
    val Add: Painter @Composable get() = painterResource(R.drawable.ic_fluent_add)
    val Calendar: Painter @Composable get() = painterResource(R.drawable.ic_fluent_calendar)
    val Search: Painter @Composable get() = painterResource(R.drawable.ic_fluent_search)
    val ChannelNotifications: Painter @Composable get() = painterResource(R.drawable.ic_fluent_channel_notifications)
    val Group: Painter @Composable get() = painterResource(R.drawable.ic_fluent_group)
    val LockShield: Painter @Composable get() = painterResource(R.drawable.ic_fluent_lock_shield)
    val QuestionCircle: Painter @Composable get() = painterResource(R.drawable.ic_fluent_question_circle)
    val Sleep: Painter @Composable get() = painterResource(R.drawable.ic_fluent_sleep)
    val PhoneVibrate: Painter @Composable get() = painterResource(R.drawable.ic_fluent_phone_vibrate)
    val DataPie: Painter @Composable get() = painterResource(R.drawable.ic_fluent_data_pie)
    val CloudSyncComplete: Painter @Composable get() = painterResource(R.drawable.ic_fluent_cloud_sync_complete)
    val FolderMove: Painter @Composable get() = painterResource(R.drawable.ic_fluent_folder_move)
    val CloudBackup: Painter @Composable get() = painterResource(R.drawable.ic_fluent_cloud_backup)
    val DocumentTopRight: Painter @Composable get() = painterResource(R.drawable.ic_fluent_document_top_right)
  }

  object Builder {
    val Details: Painter @Composable get() = painterResource(R.drawable.ic_builder_details)
    val ByMonthday: Painter @Composable get() = painterResource(R.drawable.ic_builder_by_monthday)
    val Tag: Painter @Composable get() = painterResource(R.drawable.ic_builder_group)
    val RepeatLimit: Painter @Composable get() = painterResource(R.drawable.ic_builder_repeat_limit)
    val AddCall: Painter @Composable get() = painterResource(R.drawable.ic_builder_add_call)
    val SendMessage: Painter @Composable get() = painterResource(R.drawable.ic_builder_send_message)
    val EmailAddress: Painter @Composable get() = painterResource(R.drawable.ic_builder_email_address)
    val EmailSubject: Painter @Composable get() = painterResource(R.drawable.ic_builder_email_subject)
    val AddApp: Painter @Composable get() = painterResource(R.drawable.ic_builder_add_app)
    val WebAddress: Painter @Composable get() = painterResource(R.drawable.ic_builder_web_address)
    val Clear: Painter @Composable get() = painterResource(R.drawable.ic_builder_clear)
    val Interval: Painter @Composable get() = painterResource(R.drawable.ic_builder_interval)
    val ArrowLeft: Painter @Composable get() = painterResource(R.drawable.ic_builder_arrow_left)
  }
}
