package com.elementary.tasks.core.binding.views

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding

class NavHeaderBinding(view: View) : Binding(view) {
  val appNameBanner: TextView by bindView(R.id.appNameBanner)
  val appNameBannerPro: TextView by bindView(R.id.appNameBannerPro)
  val saleBadge: TextView by bindView(R.id.sale_badge)
  val updateBadge: TextView by bindView(R.id.update_badge)
  val playServicesWarning: TextView by bindView(R.id.playServicesWarning)
  val backupBadge: TextView by bindView(R.id.backupBadge)
  val doNoDisturbIcon: ImageView by bindView(R.id.doNoDisturbIcon)
}