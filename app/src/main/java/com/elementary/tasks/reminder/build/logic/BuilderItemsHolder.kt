package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.BuilderItem

class BuilderItemsHolder {

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

  fun update(position: Int, builderItem: BuilderItem<*>) {
    items[position] = builderItem
  }

  fun remove(position: Int) {
    items.removeAt(position)
  }

  fun getItems(): List<BuilderItem<*>> {
    return items
  }
}
