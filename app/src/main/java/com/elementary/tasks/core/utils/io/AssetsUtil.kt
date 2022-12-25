package com.elementary.tasks.core.utils.io

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.elementary.tasks.R

object AssetsUtil {

  fun getFontNames(): List<String> {
    val list = mutableListOf<String>()

    list.add("Roboto Black")
    list.add("Roboto Black Italic")
    list.add("Roboto Bold")
    list.add("Roboto Bold Italic")
    list.add("Roboto Italic")
    list.add("Roboto Light")
    list.add("Roboto Light Italic")
    list.add("Roboto Medium")
    list.add("Roboto Medium Italic")
    list.add("Roboto Regular")
    list.add("Roboto Thin")
    list.add("Roboto Thin Italic")

    list.add("Merriweather Sans Bold")
    list.add("Merriweather Sans Bold Italic")
    list.add("Merriweather Sans Extra Bold")
    list.add("Merriweather Sans Extra Bold Italic")
    list.add("Merriweather Sans Italic")
    list.add("Merriweather Sans Light")
    list.add("Merriweather Sans Light Italic")
    list.add("Merriweather Sans Regular")

    list.add("Lobster Regular")

    return list
  }

  fun getTypeface(context: Context, code: Int): Typeface? {
    return when (code) {
      0 -> ResourcesCompat.getFont(context, R.font.roboto_black)
      1 -> ResourcesCompat.getFont(context, R.font.roboto_black_italic)
      2 -> ResourcesCompat.getFont(context, R.font.roboto_bold)
      3 -> ResourcesCompat.getFont(context, R.font.roboto_bold_italic)
      4 -> ResourcesCompat.getFont(context, R.font.roboto_italic)
      5 -> ResourcesCompat.getFont(context, R.font.roboto_light)
      6 -> ResourcesCompat.getFont(context, R.font.roboto_light_italic)
      7 -> ResourcesCompat.getFont(context, R.font.roboto_medium)
      8 -> ResourcesCompat.getFont(context, R.font.roboto_medium_italic)
      9 -> ResourcesCompat.getFont(context, R.font.roboto_regular)
      10 -> ResourcesCompat.getFont(context, R.font.roboto_thin)
      11 -> ResourcesCompat.getFont(context, R.font.roboto_thin_italic)
      12 -> ResourcesCompat.getFont(context, R.font.merriweathersans_bold)
      13 -> ResourcesCompat.getFont(context, R.font.merriweathersans_bolditalic)
      14 -> ResourcesCompat.getFont(context, R.font.merriweathersans_extrabold)
      15 -> ResourcesCompat.getFont(context, R.font.merriweathersans_extrabolditalic)
      16 -> ResourcesCompat.getFont(context, R.font.merriweathersans_italic)
      17 -> ResourcesCompat.getFont(context, R.font.merriweathersans_light)
      18 -> ResourcesCompat.getFont(context, R.font.merriweathersans_lightitalic)
      19 -> ResourcesCompat.getFont(context, R.font.merriweathersans_regular)
      20 -> ResourcesCompat.getFont(context, R.font.lobster_regular)
      else -> ResourcesCompat.getFont(context, R.font.roboto_regular)
    }
  }
}
