package com.elementary.tasks.qstile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.elementary.tasks.R
import com.elementary.tasks.navigation.BottomNavActivity
import com.elementary.tasks.splash.ShortcutDestination

/** One-tap "new reminder" tile, reusing the same deep-link bundle the app shortcuts use. */
class QuickAddTileService : TileService() {

  override fun onStartListening() {
    super.onStartListening()
    qsTile?.apply {
      state = Tile.STATE_ACTIVE
      label = getString(R.string.add_reminder_menu)
      icon = Icon.createWithResource(this@QuickAddTileService, R.drawable.ic_fluent_alert)
      updateTile()
    }
  }

  override fun onClick() {
    super.onClick()
    val intent =
      Intent(Intent.ACTION_MAIN)
        .setClass(this, BottomNavActivity::class.java)
        .putExtras(ShortcutDestination.createBundle(ShortcutDestination.Shortcut.Reminder))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      val pendingIntent =
        PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
      startActivityAndCollapse(pendingIntent)
    } else {
      @Suppress("DEPRECATION")
      startActivityAndCollapse(intent)
    }
  }
}
