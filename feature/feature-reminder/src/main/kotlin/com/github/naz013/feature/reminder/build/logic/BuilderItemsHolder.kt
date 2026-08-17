package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.feature.reminder.build.BuilderItem

internal class BuilderItemsHolder {
  private val items = mutableListOf<BuilderItem<*>>()

  fun addAll(builderItems: List<BuilderItem<*>>) {
    items.addAll(builderItems)
  }

  fun setAll(builderItems: List<BuilderItem<*>>) {
    items.clear()
    items.addAll(builderItems)
  }

  fun addNew(builderItem: BuilderItem<*>) {
    items.add(builderItem)
  }

  fun update(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    if (position < 0 || position >= items.size) return
    items[position] = builderItem
  }

  fun remove(position: Int) {
    if (position < 0 || position >= items.size) return
    items.removeAt(position)
  }

  fun getItems(): List<BuilderItem<*>> = items
}
