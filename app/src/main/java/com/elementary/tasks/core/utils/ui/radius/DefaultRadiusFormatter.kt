package com.elementary.tasks.core.utils.ui.radius

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.common.TextProvider
import com.elementary.tasks.core.utils.UnitConverter
import com.elementary.tasks.core.utils.ui.ValueFormatter

open class DefaultRadiusFormatter(
  context: Context,
  var useMetric: Boolean,
  private val unitConverter: UnitConverter = UnitConverter()
) : ValueFormatter<Int> {

  private val textProvider = TextProvider(context)

  override fun format(meters: Int): String {
    return if (meters > 5000) {
      if (useMetric) {
        metersToKm(meters)
      } else {
        metersToMi(meters)
      }
    } else {
      if (useMetric) {
        meters(meters)
      } else {
        metersToFt(meters)
      }
    }
  }

  private fun meters(value: Int): String {
    return textProvider.getText(R.string.radius_x_m, value.toString())
  }

  private fun metersToKm(meters: Int): String {
    val km = unitConverter.m2Km(meters.toFloat())
    return textProvider.getText(R.string.radius_x_km, String.format("%.2f", km))
  }

  private fun metersToMi(meters: Int): String {
    val mi = unitConverter.m2Mi(meters.toFloat())
    return textProvider.getText(R.string.radius_x_mi, String.format("%.2f", mi))
  }

  private fun metersToFt(meters: Int): String {
    val ft = unitConverter.m2Ft(meters.toFloat())
    return textProvider.getText(R.string.radius_x_ft, String.format("%.2f", ft))
  }
}
