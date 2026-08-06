package com.elementary.tasks.reminder.build.logic

import com.elementary.tasks.reminder.build.BuilderItem

class BuilderItemsLogic(
  private val builderItemsHolder: BuilderItemsHolder,
) {
  private var items = emptyList<BuilderItem<*>>()

  fun addAll(builderItems: List<BuilderItem<*>>) {
    builderItemsHolder.addAll(builderItems)
  }

  fun setAll(builderItems: List<BuilderItem<*>>) {
    builderItemsHolder.setAll(builderItems)
  }

  fun addNew(builderItem: BuilderItem<*>) {
    builderItemsHolder.addNew(builderItem)
  }

  fun update(
    position: Int,
    builderItem: BuilderItem<*>,
  ) {
    builderItemsHolder.update(position, builderItem)
  }

  fun remove(position: Int) {
    builderItemsHolder.remove(position)
  }

  fun canAdd(): Boolean = getAvailable().isNotEmpty()

  fun setAllAvailable(items: List<BuilderItem<*>>) {
    this.items = items
  }

  fun getUsed(): List<BuilderItem<*>> = builderItemsHolder.getItems()

  fun getAvailable(): List<BuilderItem<*>> {
    val usedTypes = builderItemsHolder.getItems().map { it.biType }.toSet()
    return items.filterNot { it.biType in usedTypes }
  }
}
