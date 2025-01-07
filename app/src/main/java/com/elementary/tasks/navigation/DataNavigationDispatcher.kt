package com.elementary.tasks.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.home.BottomNavActivity
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Birthday
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DataDestination
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.EditBirthdayScreen
import com.github.naz013.navigation.intent.IntentDataWriter
import com.github.naz013.ui.common.context.buildIntent

class DataNavigationDispatcher(
  private val context: Context,
  private val intentDataWriter: IntentDataWriter
) : NavigationDispatcher<DataDestination> {

  override fun dispatch(destination: DataDestination) {
    val data = destination.data

    val clazz = getClass(data)
    if (clazz == null) {
      Logger.e("DataNavigator", "Failed to find destination for the $data")
      return
    }

    Logger.i("DataNavigator", "Going to ${clazz.simpleName}, with $data")

    intentDataWriter.putData(IntentKeys.INTENT_ITEM, data)

    context.buildIntent(clazz) {
      getAction(data)?.also { setAction(it) }
      getFlags(data)?.also { addFlags(it) }
      getExtras(data)?.also { putExtras(it) }
    }.also {
      context.startActivity(it)
    }
  }

  private fun getExtras(data: Any): Bundle? {
    return when (data) {
      is Birthday -> {
        Bundle().apply {
          putParcelable(
            DeepLinkDestination.KEY,
            EditBirthdayScreen(
              Bundle().apply {
                putBoolean(IntentKeys.INTENT_DEEP_LINK, true)
              }
            )
          )
        }
      }

      else -> null
    }
  }

  private fun getFlags(data: Any): Int? {
    return null
  }

  private fun getAction(data: Any): String? {
    return when (data) {
      is Birthday -> Intent.ACTION_VIEW
      else -> null
    }
  }

  private fun getClass(data: Any): Class<*>? {
    return when (data) {
      is Birthday -> BottomNavActivity::class.java
      else -> null
    }
  }
}
