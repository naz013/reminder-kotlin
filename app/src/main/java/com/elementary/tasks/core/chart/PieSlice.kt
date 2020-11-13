package com.elementary.tasks.core.chart

import android.graphics.Color
import android.graphics.Path
import android.graphics.Region

class PieSlice {
  var color = Color.BLACK
  var value: Float = 0.toFloat()
  var title: String? = null
  var path: Path? = null
  var region: Region? = null
}
