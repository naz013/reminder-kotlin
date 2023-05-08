package com.elementary.tasks.core.utils

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.text.TextUtils
import androidx.core.net.toUri
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs

object ReminderUtils {

  const val DAY_CHECKED = 1

  fun getSound(context: Context, prefs: Prefs, melody: String?): Melody {
    return getSound(context, prefs.melodyFile, melody)
  }

  fun getSound(context: Context, defMelody: String, melody: String?): Melody {
    return if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody!!)) {
      val uri = UriUtil.getUri(context, melody)
      if (uri != null) {
        Melody(MelodyType.FILE, uri)
      } else {
        val ringtone = RingtoneManager.getRingtone(context, melody.toUri())
        if (ringtone != null) {
          Melody(MelodyType.RINGTONE, melody.toUri())
        } else {
          Melody(MelodyType.DEFAULT, defUri(defMelody))
        }
      }
    } else {
      if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
        val uri = UriUtil.getUri(context, defMelody)
        if (uri != null) {
          Melody(MelodyType.FILE, uri)
        } else {
          val ringtone = RingtoneManager.getRingtone(context, defMelody.toUri())
          if (ringtone != null) {
            Melody(MelodyType.RINGTONE, defMelody.toUri())
          } else {
            Melody(MelodyType.DEFAULT, defUri(defMelody))
          }
        }
      } else {
        Melody(MelodyType.DEFAULT, defUri(defMelody))
      }
    }
  }

  fun getSoundUri(context: Context, prefs: Prefs, melody: String?): Uri {
    return if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody!!)) {
      UriUtil.getUri(context, melody) ?: defUri(prefs)
    } else {
      val defMelody = prefs.melodyFile
      if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
        UriUtil.getUri(context, defMelody) ?: defUri(prefs)
      } else {
        defUri(prefs)
      }
    }
  }

  private fun defUri(prefs: Prefs): Uri {
    return defUri(prefs.melodyFile)
  }

  private fun defUri(prefsMelody: String): Uri {
    return when (prefsMelody) {
      Constants.SOUND_RINGTONE -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      Constants.SOUND_ALARM -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }
  }

  @Deprecated("Use DateTimeManager")
  fun getRepeatString(context: Context, prefs: Prefs, repCode: List<Int>): String {
    val sb = StringBuilder()
    val first = prefs.startDay
    if (first == 0 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sun))
    }
    if (repCode[1] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.mon))
    }
    if (repCode[2] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.tue))
    }
    if (repCode[3] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.wed))
    }
    if (repCode[4] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.thu))
    }
    if (repCode[5] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.fri))
    }
    if (repCode[6] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sat))
    }
    if (first == 1 && repCode[0] == DAY_CHECKED) {
      sb.append(" ")
      sb.append(context.getString(R.string.sun))
    }
    return if (isAllChecked(repCode)) {
      context.getString(R.string.everyday)
    } else {
      sb.toString().trim()
    }
  }

  private fun isAllChecked(repCode: List<Int>): Boolean {
    return repCode.none { it == 0 }
  }

  enum class MelodyType {
    DEFAULT,
    RINGTONE,
    FILE
  }

  data class Melody(val melodyType: MelodyType, val uri: Uri)
}
