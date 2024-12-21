package com.github.naz013.repository.observer

import com.github.naz013.repository.table.Table

interface TableChangeListenerFactory {
  fun create(table: Table, onChanged: () -> Unit): TableChangeListener
}
