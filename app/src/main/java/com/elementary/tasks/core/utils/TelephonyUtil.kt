package com.elementary.tasks.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.UiShareData
import com.github.naz013.common.uri.UriUtil
import java.io.File

object TelephonyUtil {

  fun sendNote(file: File, context: Context, message: String?) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    var title = "Note"
    var note = ""
    if (message != null) {
      if (message.length > 100) {
        title = message.take(48)
        title = "$title..."
      }
      if (message.length > 150) {
        note = message.take(135)
        note = "$note..."
      }
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, title)
    intent.putExtra(Intent.EXTRA_TEXT, note)
    val uri = UriUtil.getUri(context, file, BuildConfig.APPLICATION_ID)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    try {
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_send_email)))
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendFile(file: File, context: Context, message: String?) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "*/*"
    intent.putExtra(Intent.EXTRA_SUBJECT, message)
    val uri = UriUtil.getUri(context, file, BuildConfig.APPLICATION_ID)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
      val chooser = Intent.createChooser(intent, context.getString(R.string.share_send_email))
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendFile(context: Context, shareData: UiShareData) {
    if (shareData.file == null) return
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "*/*"
    intent.putExtra(Intent.EXTRA_SUBJECT, shareData.name)
    val uri = UriUtil.getUri(context, shareData.file, BuildConfig.APPLICATION_ID)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
      val chooser = Intent.createChooser(intent, context.getString(R.string.share_send_email))
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendMail(
    context: Context,
    email: String,
    subject: String,
    message: String,
    filePath: String?
  ) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, message)
    if (filePath != null) {
      val uri = UriUtil.getUri(context, filePath, BuildConfig.APPLICATION_ID)
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    try {
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_send_email)))
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendMail(
    context: Context,
    email: String,
    subject: String,
    message: String,
    file: File?
  ) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, message)
    if (file != null) {
      val uri = UriUtil.getUri(context, file, BuildConfig.APPLICATION_ID)
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    try {
      context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_send_email)))
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendSms(number: String, context: Context) {
    if (TextUtils.isEmpty(number)) {
      return
    }
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("sms:$number")
    smsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      context.startActivity(smsIntent)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun sendSms(context: Context, number: String, message: String?) {
    if (TextUtils.isEmpty(number)) {
      return
    }
    val smsIntent = Intent(Intent.ACTION_VIEW)
    smsIntent.data = Uri.parse("sms:$number")
    smsIntent.putExtra("sms_body", message)
    smsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      context.startActivity(smsIntent)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun makeCall(number: String, context: Context) {
    if (TextUtils.isEmpty(number)) {
      return
    }
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:$number")
    callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
      context.startActivity(callIntent)
    } catch (e: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun openApp(appPackage: String, context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
    try {
      context.startActivity(launchIntent)
    } catch (ignored: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun openLink(link: String, context: Context) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    try {
      context.startActivity(browserIntent)
    } catch (ignored: Exception) {
      Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
    }
  }

  fun isPhoneNumber(target: String): Boolean {
    val phonePattern = "^[+]?[0-9 ()-]{3,25}\$".toRegex()
    return phonePattern.matches(target)
  }
}
