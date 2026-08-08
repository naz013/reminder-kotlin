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
    // A fast double-tap on the "+" selector can invoke this twice for the same biType before the
    // first add propagates back and flips that entry to unavailable in the sheet. Since only one
    // item per biType is ever meant to be used, guard here rather than relying on the UI timing -
    // a second item with the same biType would collide as a LazyColumn key in BuildReminderScreen.
    if (builderItemsHolder.getItems().any { it.biType == builderItem.biType }) return
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
