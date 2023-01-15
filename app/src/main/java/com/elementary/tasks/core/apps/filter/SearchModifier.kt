package com.elementary.tasks.core.apps.filter

abstract class SearchModifier<V>(
  modifier: Modifier<V>? = null,
  callback: ((List<V>) -> Unit)? = null
) : Modifier<V>(modifier, callback) {

  var searchValue: String = ""
    private set

  override fun apply(data: List<V>): List<V> {
    val list = mutableListOf<V>()
    for (note in super.apply(data)) {
      if (filter(note)) list.add(note)
    }
    return list
  }

  abstract fun filter(v: V): Boolean

  fun setSearchValue(value: String?) {
    searchValue = value ?: ""
    onChanged()
  }
}