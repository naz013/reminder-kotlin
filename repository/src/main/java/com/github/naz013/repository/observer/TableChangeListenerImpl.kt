package com.github.naz013.repository.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.naz013.logging.Logger
import com.github.naz013.repository.table.Table

internal class TableChangeListenerImpl(
  private val context: Context,
  private val table: Table,
  private val onChanged: () -> Unit
) : TableChangeListener {

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val intentTable = intent.getStringExtra(TableChangeAction.TABLE)
      if (intentTable != null && intentTable == table.tableName) {
        Logger.i("Table change detected for table: ${table.tableName}")
        onChanged()
      }
    }
  }

  override fun register() {
    Logger.i("Registering table change listener for table: ${table.tableName}")
    LocalBroadcastManager.getInstance(context).registerReceiver(
      receiver,
      IntentFilter(TableChangeAction.ACTION)
    )
  }

  override fun unregister() {
    Logger.i("Unregistering table change listener for table: ${table.tableName}")
    LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
  }
}
