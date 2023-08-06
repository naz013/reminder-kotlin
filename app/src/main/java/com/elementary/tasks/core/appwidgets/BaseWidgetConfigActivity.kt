package com.elementary.tasks.core.appwidgets

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.arch.BindingActivity
import org.koin.android.ext.android.inject

abstract class BaseWidgetConfigActivity<B : ViewBinding> : BindingActivity<B>() {
  protected val analyticsEventSender by inject<AnalyticsEventSender>()
}
