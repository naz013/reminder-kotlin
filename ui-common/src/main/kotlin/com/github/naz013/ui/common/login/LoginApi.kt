package com.github.naz013.ui.common.login

import android.content.Context
import android.content.Intent
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.ui.common.activity.DeepLinkData
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.intentForClass
import com.github.naz013.ui.common.context.startActivity

object LoginApi {

  internal const val ARG_BACK = "arg_back"
  internal const val ARG_LOGGED = "arg_logged"

  fun Intent.isLogged(): Boolean {
    return getBooleanExtra(PinLoginActivity.ARG_LOGGED, false)
  }

  fun authIntent(context: Context): Intent {
    return context.buildIntent(PinLoginActivity::class.java) {
      putExtra(ARG_BACK, true)
    }
  }

  fun openLogged(context: Context, clazz: Class<*>) {
    context.startActivity(clazz) {
      putExtra(ARG_LOGGED, true)
    }
  }

  fun openLogged(
    context: Context,
    clazz: Class<*>,
    deepLinkData: DeepLinkData,
    builder: Intent.() -> Unit
  ) {
    val intent = context.intentForClass(clazz).apply {
      builder(this)
      putExtra(ARG_LOGGED, true)
      putExtra(IntentKeys.INTENT_DEEP_LINK, true)
      putExtra(deepLinkData.intentKey, deepLinkData)
    }
    context.startActivity(intent)
  }

  fun openLogged(context: Context, clazz: Class<*>, builder: Intent.() -> Unit) {
    val intent = context.intentForClass(clazz).apply {
      builder(this)
      putExtra(ARG_LOGGED, true)
    }
    context.startActivity(intent)
  }
}
