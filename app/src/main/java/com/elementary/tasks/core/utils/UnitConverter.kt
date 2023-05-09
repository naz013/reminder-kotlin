package com.elementary.tasks.core.utils

class UnitConverter {
  fun m2Ft(meters: Float): Float {
    return (meters * 3.2808399).toFloat()
  }

  fun m2Mi(meters: Float): Float {
    return (meters * 0.000621371192).toFloat()
  }

  fun m2Km(meters: Float): Float {
    return meters / 1000f
  }
}
