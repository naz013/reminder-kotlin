package com.elementary.tasks.core.os

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.os.data.UiPermissionDialogData
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.Dialogues
import java.util.LinkedList

class PermissionFlow private constructor(
  private val launcher: Launcher,
  private val dialogues: Dialogues
) {

  private var askedPermission = ""
  private val map = mutableMapOf<String, Boolean>()
  private val queue: LinkedList<String> = LinkedList()

  private var permissionGrantedCallback: ((permission: String) -> Unit)? = null
  private var permissionsGrantedCallback: ((permissions: List<String>) -> Unit)? =
    null
  private var permissionDeniedCallback: ((permission: String) -> Unit)? = null

  init {
    launcher.subscribe(
      onGranted = { permissionGranted(askedPermission) },
      onDenied = { permissionDeniedCallback?.invoke(it) }
    )
  }

  constructor(fragment: Fragment, dialogues: Dialogues) : this(
    FragmentLauncher(fragment),
    dialogues
  )

  constructor(activity: ComponentActivity, dialogues: Dialogues) : this(
    ActivityLauncher(activity),
    dialogues
  )

  fun askPermission(
    permission: String,
    callback: (permission: String) -> Unit
  ) {
    if (permission == Permissions.POST_NOTIFICATION && !Module.is13) {
      callback.invoke(permission)
      return
    }
    if (permission == Permissions.FOREGROUND_SERVICE && !Module.isPie) {
      callback.invoke(permission)
      return
    }
    if (permission == Permissions.BACKGROUND_LOCATION && !Module.is10) {
      callback.invoke(permission)
      return
    }

    this.map.clear()
    this.queue.clear()
    this.permissionDeniedCallback = null
    this.permissionGrantedCallback = callback
    this.permissionsGrantedCallback = null
    checkPermission(permission)
  }

  fun askPermission(
    permission: String,
    callback: (permission: String) -> Unit,
    deniedCallback: (permission: String) -> Unit
  ) {
    this.permissionDeniedCallback = deniedCallback
    askPermission(permission, callback)
  }

  fun askPermissions(
    permissions: List<String>,
    callback: (permissions: List<String>) -> Unit
  ) {
    this.map.clear()
    this.queue.clear()
    this.permissionsGrantedCallback = callback
    this.permissionGrantedCallback = null
    this.permissionDeniedCallback = null
    this.queue.addAll(permissions)

    queue.poll()?.let { checkPermission(it) }
  }

  fun askPermissions(
    permissions: List<String>,
    callback: (permissions: List<String>) -> Unit,
    deniedCallback: (permission: String) -> Unit
  ) {
    this.map.clear()
    this.queue.clear()
    this.permissionsGrantedCallback = callback
    this.permissionGrantedCallback = null
    this.permissionDeniedCallback = deniedCallback
    this.queue.addAll(permissions)

    queue.poll()?.let { checkPermission(it) }
  }

  private fun checkPermission(permission: String) {
    when (permission) {
      Permissions.POST_NOTIFICATION -> if (!Module.is13) {
        permissionGranted(permission)
        return
      }
      Permissions.BACKGROUND_LOCATION -> if (!Module.is10) {
        permissionGranted(permission)
        return
      }
      Permissions.FOREGROUND_SERVICE -> if (!Module.isPie) {
        permissionGranted(permission)
        return
      }
    }
    this.askedPermission = permission
    when {
      ContextCompat.checkSelfPermission(
        launcher.getActivity(),
        permission
      ) == PackageManager.PERMISSION_GRANTED -> {
        permissionGranted(permission)
      }

      shouldShowRequestPermissionRationale(launcher.getActivity(), permission) -> {
        explainPermission(permission)
      }

      else -> {
        launcher.launch(permission)
      }
    }
  }

  private fun permissionGranted(permission: String) {
    map[permission] = true
    if (queue.isEmpty()) {
      notifyPermissionResult()
    } else {
      queue.poll()?.let { checkPermission(it) } ?: run { notifyPermissionResult() }
    }
  }

  private fun notifyPermissionResult() {
    if (map.isNotEmpty()) {
      if (map.size == 1) {
        permissionGrantedCallback?.invoke(askedPermission)
      } else {
        permissionsGrantedCallback?.invoke(map.keys.toList())
      }
    }
  }

  private fun explainPermission(permission: String) {
    val dialogData = when (permission) {
      Permissions.POST_NOTIFICATION -> if (Module.is13) {
        UiPermissionDialogData.POST_NOTIFICATION
      } else {
        null
      }

      Permissions.READ_CONTACTS -> UiPermissionDialogData.READ_CONTACTS
      Permissions.GET_ACCOUNTS -> UiPermissionDialogData.GET_ACCOUNTS
      Permissions.CALL_PHONE -> UiPermissionDialogData.CALL_PHONE
      Permissions.READ_CALENDAR -> UiPermissionDialogData.READ_CALENDAR
      Permissions.WRITE_CALENDAR -> UiPermissionDialogData.WRITE_CALENDAR
      Permissions.READ_EXTERNAL -> UiPermissionDialogData.READ_EXTERNAL
      Permissions.WRITE_EXTERNAL -> UiPermissionDialogData.WRITE_EXTERNAL
      Permissions.ACCESS_FINE_LOCATION -> UiPermissionDialogData.FINE_LOCATION
      Permissions.ACCESS_COARSE_LOCATION -> UiPermissionDialogData.COARSE_LOCATION
      Permissions.RECORD_AUDIO -> UiPermissionDialogData.RECORD_AUDIO
      Permissions.BACKGROUND_LOCATION -> if (Module.is10) {
        UiPermissionDialogData.BACKGROUND_LOCATION
      } else {
        null
      }

      Permissions.READ_PHONE_STATE -> UiPermissionDialogData.READ_PHONE_STATE
      Permissions.FOREGROUND_SERVICE -> if (Module.isPie) {
        UiPermissionDialogData.FOREGROUND_SERVICE
      } else {
        null
      }

      else -> null
    } ?: return

    showPermissionExplanation(dialogData)
  }

  private fun requestPermissionAfterRationale(permission: String) {
    launcher.launch(permission)
  }

  private fun showPermissionExplanation(dialogData: UiPermissionDialogData) {
    dialogues.getMaterialDialog(launcher.getActivity())
      .setTitle(dialogData.title)
      .setMessage(dialogData.description)
      .setPositiveButton(R.string.ok) { di, _ ->
        di.dismiss()
        requestPermissionAfterRationale(dialogData.permission)
      }
      .create()
      .show()
  }

  abstract class Launcher {
    protected var askedPermission = ""
    protected lateinit var onGranted: (String) -> Unit
    protected lateinit var onDenied: (String) -> Unit

    fun subscribe(onGranted: (String) -> Unit, onDenied: (String) -> Unit) {
      this.onGranted = onGranted
      this.onDenied = onDenied
    }

    abstract fun launch(permission: String)

    abstract fun getActivity(): Activity
  }

  class ActivityLauncher(private val activity: ComponentActivity) : Launcher() {

    private val permissionLauncher =
      activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
          onGranted.invoke(askedPermission)
        } else {
          onDenied.invoke(askedPermission)
        }
      }

    override fun getActivity(): Activity {
      return activity
    }

    override fun launch(permission: String) {
      permissionLauncher.launch(permission)
    }
  }

  class FragmentLauncher(
    private val fragment: Fragment
  ) : Launcher() {

    private val permissionLauncher =
      fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
          onGranted.invoke(askedPermission)
        } else {
          onDenied.invoke(askedPermission)
        }
      }

    override fun getActivity(): Activity {
      return fragment.requireActivity()
    }

    override fun launch(permission: String) {
      permissionLauncher.launch(permission)
    }
  }
}
