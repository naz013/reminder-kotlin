package com.elementary.tasks.core.arch

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.os.PermissionFlow
import com.github.naz013.ui.common.Dialogues
import org.koin.android.ext.android.inject

abstract class BindingFragment<B : ViewBinding> : Fragment() {

  protected val dialogues by inject<Dialogues>()
  protected lateinit var permissionFlow: PermissionFlow

  protected lateinit var binding: B

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    permissionFlow = PermissionFlow(this, dialogues)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = inflate(inflater, container, savedInstanceState)
    return binding.root
  }

  protected fun string(@StringRes res: Int): String {
    return if (context != null && isAdded) {
      getString(res)
    } else {
      ""
    }
  }

  abstract fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): B

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
