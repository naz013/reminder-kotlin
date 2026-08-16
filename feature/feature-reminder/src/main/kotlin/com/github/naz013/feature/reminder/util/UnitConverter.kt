package com.github.naz013.feature.reminder.util

class UnitConverter {
  fun m2Ft(meters: Float): Float = (meters * 3.2808399).toFloat()

  fun m2Mi(meters: Float): Float = (meters * 0.000621371192).toFloat()

  fun m2Km(meters: Float): Float = meters / 1000f
}
