package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.elementary.tasks.R

object IntentUtil {

  @RequiresPermission(Permissions.READ_EXTERNAL)
  fun pickImage(activity: Activity, code: Int) {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    try {
      activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_image)), code)
    } catch (e: Exception) {
    }
  }

  @RequiresPermission(Permissions.READ_EXTERNAL)
  fun pickMelody(activity: Activity, code: Int) {
    val intent = Intent()
    intent.type = "audio/*"
    intent.action = Intent.ACTION_GET_CONTENT
    try {
      activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.select_melody)), code)
    } catch (e: Exception) {
    }
  }

  @RequiresPermission(Permissions.READ_EXTERNAL)
  fun pickFile(activity: Activity, code: Int) {
    val intent = Intent()
    intent.type = "*/*"
    intent.action = Intent.ACTION_GET_CONTENT
    try {
      activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.choose_file)), code)
    } catch (e: Exception) {
    }
  }
}