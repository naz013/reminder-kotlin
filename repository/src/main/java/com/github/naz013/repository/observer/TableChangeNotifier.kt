package com.github.naz013.repository.observer

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.naz013.logging.Logger
import com.github.naz013.repository.table.Table

internal class TableChangeNotifier(
  private val context: Context
) {

  fun notify(table: Table) {
    Logger.i("TableChangeNotifier", "Notifying about table ${table.name}")
    LocalBroadcastManager.getInstance(context).sendBroadcast(
      Intent(TableChangeAction.ACTION).apply {
        putExtra(TableChangeAction.TABLE, table.name)
      }
    )
  }
}
