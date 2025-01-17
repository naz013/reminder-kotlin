package com.github.naz013.feature.common

import android.database.Cursor

fun Cursor.readString(columnName: String): String? {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getString(it) }
}

fun Cursor.readString(columnName: String, def: String): String {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getString(it) } ?: def
}

fun Cursor.readLong(columnName: String): Long? {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getLong(it) }
}

fun Cursor.readLong(columnName: String, def: Long): Long {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getLong(it) } ?: def
}

fun Cursor.readInt(columnName: String): Int? {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getInt(it) }
}

fun Cursor.readInt(columnName: String, def: Int): Int {
  return getColumnIndex(columnName).takeIf { it >= 0 }?.let { getInt(it) } ?: def
}
