package com.elementary.tasks.core.arch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.ActivityDestination
import com.github.naz013.navigation.DestinationScreen
import com.github.naz013.navigation.Navigator
import com.github.naz013.ui.common.activity.LightThemedActivity
import org.koin.android.ext.android.inject

class CreateNoteIntentActivity : LightThemedActivity() {
  private val navigator by inject<Navigator>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val action = intent?.action
    val type = intent?.type
    Logger.i(TAG, "Incoming intent with action: $action, type: $type")

    val extras = Bundle()
    when {
      action == Intent.ACTION_SEND && "text/plain" == type -> {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (text == null) {
          finish()
          return
        }
        extras.putString(Intent.EXTRA_TEXT, text)
      }

      action == Intent.ACTION_SEND && type?.startsWith("image/") == true -> {
        val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        if (uri == null) {
          finish()
          return
        }
        extras.putParcelableArrayList(Intent.EXTRA_STREAM, arrayListOf(uri))
      }

      action == Intent.ACTION_SEND_MULTIPLE && type?.startsWith("image/") == true -> {
        val uris =
          intent
            .getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
            ?.filterNotNull()
            ?.filterIsInstance<Uri>()
        if (uris.isNullOrEmpty()) {
          finish()
          return
        }
        extras.putParcelableArrayList(Intent.EXTRA_STREAM, ArrayList(uris))
      }

      action == ACTION_ARC_CREATE_NOTE -> {
        // Opens a blank note, no extras needed.
      }

      else -> {
        Logger.i(TAG, "Unsupported action")
        finish()
        return
      }
    }

    navigator.navigate(
      ActivityDestination(
        screen = DestinationScreen.NoteCreate,
        extras = extras,
        flags = Intent.FLAG_ACTIVITY_NEW_TASK,
        isLoggedIn = true,
        action = Intent.ACTION_VIEW,
      ),
    )
  }

  companion object {
    private const val TAG = "CreateNoteIntentActivity"
    private const val ACTION_ARC_CREATE_NOTE = "org.chromium.arc.intent.action.CREATE_NOTE"
  }
}
