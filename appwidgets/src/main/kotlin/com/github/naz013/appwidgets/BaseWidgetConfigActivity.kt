package com.github.naz013.appwidgets

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.viewbinding.ViewBinding
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.ui.common.activity.BindingActivity
import org.koin.android.ext.android.inject

internal abstract class BaseWidgetConfigActivity<B : ViewBinding> : BindingActivity<B>() {
  protected val analyticsEventSender by inject<AnalyticsEventSender>()

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
  }
}
