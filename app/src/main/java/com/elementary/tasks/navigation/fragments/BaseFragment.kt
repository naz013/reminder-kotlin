package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.arch.ThemedActivity
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.Logger
import com.elementary.tasks.navigation.FragmentCallback
import org.koin.android.ext.android.inject

abstract class BaseFragment<B : ViewBinding> : BindingFragment<B>() {

  var callback: FragmentCallback? = null
    private set

  protected val currentStateHolder by inject<CurrentStateHolder>()
  protected val prefs = currentStateHolder.preferences
  protected val dialogues by inject<Dialogues>()
  protected val notifier = currentStateHolder.notifier
  protected val isDark = currentStateHolder.theme.isDark
  private var mLastAlpha: Float = 0f
  private var askedPermission: String = ""
  private var permissionRequestCode: Int = 0

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        permissionGranted(askedPermission, permissionRequestCode)
      } else {
        permissionWasNotGranted(askedPermission, permissionRequestCode)
      }
    }

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
    val activity = activity
    if (activity is ThemedActivity) {
      activity.invokeBackPress()
    } else {
      activity?.onBackPressed()
    }
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

  protected fun askPermission(permission: String, requestCode: Int = 0) {
    this.askedPermission = permission
    this.permissionRequestCode = requestCode
    when {
      ContextCompat.checkSelfPermission(
        requireContext(),
        permission
      ) == PackageManager.PERMISSION_GRANTED -> {
        permissionGranted(permission, permissionRequestCode)
      }

      shouldShowRequestPermissionRationale(permission) -> {
        explainPermission(permission, permissionRequestCode)
      }

      else -> {
        requestPermissionLauncher.launch(permission)
      }
    }
  }

  protected fun showPermissionExplanation(
    permission: String,
    requestCode: Int,
    title: String,
    message: String
  ) {
    withContext {
      dialogues.getMaterialDialog(it)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(R.string.ok) { di, _ ->
          di.dismiss()
          requestPermissionAfterRationale(permission, requestCode)
        }
        .create()
        .show()
    }
  }

  private fun requestPermissionAfterRationale(permission: String, requestCode: Int) {
    if (askedPermission == permission && requestCode == permissionRequestCode) {
      requestPermissionLauncher.launch(permission)
    }
  }

  protected open fun permissionGranted(permission: String, requestCode: Int) {
    Logger.d("Permission granted $permission, code=$requestCode")
  }

  protected open fun permissionWasNotGranted(permission: String, requestCode: Int) {
    Logger.d("Permission Not granted $permission, code=$requestCode")
  }

  protected open fun explainPermission(permission: String, requestCode: Int) {
    Logger.d("Explain $permission, code=$requestCode")
  }

  protected fun addMenu(
    menuRes: Int,
    onMenuItemListener: (MenuItem) -> Boolean,
    menuModifier: ((Menu) -> Unit)? = null
  ) {
    val menuHost: MenuHost = requireActivity()
    menuHost.addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(menuRes, menu)
        menuModifier?.invoke(menu)
      }

      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return onMenuItemListener(menuItem)
      }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
  }

  companion object {
    const val NESTED_SCROLL_MAX = 255f
  }
}
