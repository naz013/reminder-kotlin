package com.elementary.tasks.core.utils.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.views.TextDrawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.*

object BitmapUtils {

  private const val REQUIRED_SIZE = 350
  private val COLORS = arrayOf(
    Color.parseColor("#F44336"),
    Color.parseColor("#9C27B0"),
    Color.parseColor("#673AB7"),
    Color.parseColor("#3F51B5"),
    Color.parseColor("#2196F3"),
    Color.parseColor("#03A9F4"),
    Color.parseColor("#00BCD4"),
    Color.parseColor("#009688"),
    Color.parseColor("#388E3C"),
    Color.parseColor("#8BC34A"),
    Color.parseColor("#CDDC39"),
    Color.parseColor("#FFEB3B"),
    Color.parseColor("#FFC107"),
    Color.parseColor("#FF9800"),
    Color.parseColor("#FF5722"),
    Color.parseColor("#E91E63")
  )

  fun imageFromName(userName: String?): Drawable? {
    if (userName.isNullOrEmpty()) {
      return null
    }
    val words = userName.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val init: String
    if (words.size >= 2) {
      val word1 = words[0]
      val word2 = words[1]
      init = if (word1.isNotEmpty() && word2.isNotEmpty()) {
        word1.uppercase().substring(0, 1) + word2.uppercase().substring(0, 1)
      } else if (word1.length > 1) {
        word1.uppercase().substring(0, 2)
      } else if (word2.length > 1) {
        word2.uppercase().substring(0, 2)
      } else {
        val w = userName.uppercase()
        if (w.length > 1) {
          w.substring(0, 2)
        } else {
          w
        }
      }
    } else {
      val w = userName.uppercase()
      init = if (w.length > 1) {
        w.substring(0, 2)
      } else {
        w
      }
    }
    Timber.d("imageFromName: %s", init)
    return TextDrawable.builder()
      .beginConfig()
      .width(150)
      .height(150)
      .textColor(Color.WHITE)
      .useFont(Typeface.DEFAULT)
      .bold()
      .toUpperCase()
      .endConfig()
      .buildRound(init, COLORS[Random().nextInt(COLORS.size)])
  }

  fun imageFromName(userName: String?, callback: ((Drawable?) -> Unit)?) {
    if (userName == null || userName.isEmpty()) {
      callback?.invoke(null)
      return
    }
    launchDefault {
      val words = userName.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      val init: String
      if (words.size >= 2) {
        val word1 = words[0]
        val word2 = words[1]
        init = if (word1.isNotEmpty() && word2.isNotEmpty()) {
          word1.toUpperCase().substring(0, 1) + word2.toUpperCase().substring(0, 1)
        } else if (word1.length > 1) {
          word1.toUpperCase().substring(0, 2)
        } else if (word2.length > 1) {
          word2.toUpperCase().substring(0, 2)
        } else {
          val w = userName.toUpperCase()
          if (w.length > 1) {
            w.substring(0, 2)
          } else {
            w
          }
        }
      } else {
        val w = userName.toUpperCase()
        init = if (w.length > 1) {
          w.substring(0, 2)
        } else {
          w
        }
      }
      Timber.d("imageFromName: %s", init)
      val drawable = TextDrawable.builder()
        .beginConfig()
        .width(150)
        .height(150)
        .textColor(Color.WHITE)
        .useFont(Typeface.DEFAULT)
        .bold()
        .toUpperCase()
        .endConfig()
        .buildRound(init, COLORS[Random().nextInt(COLORS.size)])
      withUIContext { callback?.invoke(drawable) }
    }
  }

  fun getDescriptor(drawable: Drawable): BitmapDescriptor {
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
  }

  @Throws(FileNotFoundException::class)
  fun decodeUriToBitmap(context: Context, selectedImage: Uri): Bitmap {
    val o = BitmapFactory.Options()
    o.inJustDecodeBounds = true
    BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o)
    var widthTmp = o.outWidth
    var heightTmp = o.outHeight
    var scale = 1
    while (true) {
      if (widthTmp / 2 < REQUIRED_SIZE || heightTmp / 2 < REQUIRED_SIZE) {
        break
      }
      widthTmp /= 2
      heightTmp /= 2
      scale *= 2
    }
    val o2 = BitmapFactory.Options()
    o2.inSampleSize = scale
    return BitmapFactory.decodeStream(context.contentResolver.openInputStream(selectedImage), null, o2)
      ?: throw FileNotFoundException()
  }
}
