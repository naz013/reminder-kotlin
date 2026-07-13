package com.github.naz013.ui.common.login

import android.content.Context
import android.content.Intent
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.ui.common.context.intentForClass

object LoginApi {

  private const val ARG_BACK = "arg_back"
  private const val ARG_LOGGED = "arg_logged"

  fun Intent.isLogged(): Boolean {
    return getBooleanExtra(PinLoginActivity.ARG_LOGGED, false)
  }

  fun authIntent(context: Context, isBack: Boolean = true): Intent {
    return context.buildIntent(PinLoginActivity::class.java) {
      putExtra(ARG_BACK, isBack)
    }
  }

  fun openLogged(context: Context, clazz: Class<*>, builder: Intent.() -> Unit) {
    val intent = context.intentForClass(clazz).apply {
      builder(this)
      putExtra(ARG_LOGGED, true)
    }
    context.startActivity(intent)
  }
}
