package com.github.naz013.common.system

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.github.naz013.common.TextProvider
import com.github.naz013.platform.StringApi

class StringApiImpl(
  private val textProvider: TextProvider
) : StringApi {

  override fun getString(@StringRes id: Int): String {
    return textProvider.getString(id)
  }

  override fun getString(@StringRes id: Int, vararg args: Any): String {
    return textProvider.getString(id, *args)
  }

  override fun getStringArray(@ArrayRes id: Int): Array<String> {
    return textProvider.getStringArray(id)
  }
}
