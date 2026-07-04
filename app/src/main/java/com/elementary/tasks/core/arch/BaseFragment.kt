package com.elementary.tasks.core.arch

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.elementary.tasks.core.os.PermissionFlow
import com.github.naz013.ui.common.Dialogues
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment() {
  protected val dialogues by inject<Dialogues>()
  protected lateinit var permissionFlow: PermissionFlow

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
  }

  protected fun string(
    @StringRes res: Int,
  ): String =
    if (context != null && isAdded) {
      getString(res)
    } else {
      ""
    }

  protected fun withActivity(action: (Activity) -> Unit) {
    activity?.let {
      action.invoke(it)
    }
  }

  protected fun withContext(action: (Context) -> Unit) {
    context?.let {
      action.invoke(it)
    }
  }

  protected fun safeContext(action: Context.() -> Unit) {
    context?.run { action.invoke(this) }
  }
}
