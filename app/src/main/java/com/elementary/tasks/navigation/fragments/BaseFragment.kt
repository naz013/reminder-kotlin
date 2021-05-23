package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Context
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.navigation.FragmentCallback
import org.koin.android.ext.android.inject

abstract class BaseFragment<B : ViewBinding> : BindingFragment<B>() {

  var callback: FragmentCallback? = null
    private set

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val dialogues by inject<Dialogues>()
  protected val isDark = currentStateHolder.theme.isDark
  private var mLastAlpha: Float = 0f

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (callback == null) {
      try {
        callback = context as FragmentCallback?
      } catch (e: ClassCastException) {
      }
    }
  }

  protected fun toAlpha(scroll: Float, max: Float = 255f): Float = scroll / max

  protected fun setToolbarAlpha(alpha: Float) {
    if (isRemoving) return
    this.mLastAlpha = alpha
    callback?.onAlphaUpdate(alpha)
  }

  protected fun moveBack() {
    activity?.onBackPressed()
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

  open fun canGoBack(): Boolean = true

  open fun onBackStackResume() {
    callback?.setCurrentFragment(this)
    callback?.onTitleChange(getTitle())
    setToolbarAlpha(mLastAlpha)
  }

  override fun onResume() {
    super.onResume()
    onBackStackResume()
  }

  abstract fun getTitle(): String

  protected fun navigate(function: () -> NavDirections) {
    safeNavigation(function.invoke())
  }

  protected fun safeNavigation(navDirections: NavDirections) {
    safeNavigation {
      findNavController().navigate(navDirections)
    }
  }

  protected fun safeNavigation(function: () -> Unit) {
    try {
      function.invoke()
    } catch (e: Exception) {
    }
  }

  companion object {
    const val NESTED_SCROLL_MAX = 255f
  }
}
