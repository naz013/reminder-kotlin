package com.github.naz013.repository.observer

import android.content.Context
import com.github.naz013.repository.table.Table

internal class TableChangeListenerFactoryImpl(
  private val context: Context
) : TableChangeListenerFactory {
  override fun create(table: Table, onChanged: () -> Unit): TableChangeListener {
    return TableChangeListenerImpl(
      context = context,
      table = table,
      onChanged = onChanged
    )
  }
}
