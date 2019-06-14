package com.elementary.tasks.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.elementary.tasks.R
import java.io.File

object TelephonyUtil {

    fun sendNote(file: File, context: Context, message: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        var title = "Note"
        var note = ""
        if (message != null) {
            if (message.length > 100) {
                title = message.substring(0, 48)
                title = "$title..."
            }
            if (message.length > 150) {
                note = message.substring(0, 135)
                note = "$note..."
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, note)
        val uri = UriUtil.getUri(context, file)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            context.startActivity(Intent.createChooser(intent, "Send email..."))
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun sendFile(file: File, context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, file.name)
        val uri = UriUtil.getUri(context, file)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            context.startActivity(Intent.createChooser(intent, "Send email..."))
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun sendMail(context: Context, email: String, subject: String,
                 message: String, filePath: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        if (filePath != null) {
            val uri = UriUtil.getUri(context, filePath)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send email..."))
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

    @RequiresPermission(value = Permissions.CALL_PHONE)
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

    fun skypeCall(number: String, context: Context) {
        val uri = "skype:$number?call"
        val sky = Intent("android.intent.action.VIEW")
        sky.data = Uri.parse(uri)
        try {
            context.startActivity(sky)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun skypeVideoCall(number: String, context: Context) {
        val uri = "skype:$number?call&video=true"
        val sky = Intent("android.intent.action.VIEW")
        sky.data = Uri.parse(uri)
        try {
            context.startActivity(sky)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun skypeChat(number: String, context: Context) {
        val uri = "skype:$number?chat"
        val sky = Intent("android.intent.action.VIEW")
        sky.data = Uri.parse(uri)
        try {
            context.startActivity(sky)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.app_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
