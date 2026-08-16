package com.github.naz013.ui.map

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.format.UnitConverter
import com.github.naz013.ui.common.format.ValueFormatter
import com.github.naz013.common.TextProvider

open class DefaultRadiusFormatter private constructor(
  private val textProvider: TextProvider,
  var useMetric: Boolean,
  private val unitConverter: UnitConverter = UnitConverter(),
) : ValueFormatter<Int> {
  constructor(
    context: Context,
    useMetric: Boolean,
    unitConverter: UnitConverter = UnitConverter(),
  ) : this(TextProvider(context), useMetric, unitConverter)

  constructor(
    textProvider: TextProvider,
    useMetric: Boolean,
  ) : this(textProvider, useMetric, UnitConverter())

  override fun format(meters: Int): String =
    if (meters > 5000) {
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

  private fun meters(value: Int): String = textProvider.getText(R.string.radius_x_m, value.toString())

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
