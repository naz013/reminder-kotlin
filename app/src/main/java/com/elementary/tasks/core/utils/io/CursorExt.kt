package com.elementary.tasks.core.utils.io

import android.database.Cursor

fun Cursor.readString(columnName: String): String? {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getString(it) }
}

fun Cursor.readLong(columnName: String): Long? {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getLong(it) }
}
