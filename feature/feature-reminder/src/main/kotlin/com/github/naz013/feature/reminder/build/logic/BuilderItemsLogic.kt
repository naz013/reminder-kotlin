package com.github.naz013.feature.reminder.build.logic

import com.github.naz013.feature.reminder.build.BuilderItem

internal class BuilderItemsLogic(
  private val builderItemsHolder: BuilderItemsHolder,
) {
  private var items = emptyList<BuilderItem<*>>()

  fun addAll(builderItems: List<BuilderItem<*>>) {
    builderItemsHolder.addAll(builderItems)
  }

  fun setAll(builderItems: List<BuilderItem<*>>) {
    // Same invariant as addNew(): getUsed() must never contain two items with the same biType,
    // since BuildReminderScreen keys its LazyColumn rows by biType. Unlike addNew() this list can
    // come from decomposing a persisted reminder (ReminderToBiDecomposer) - a reminder whose
    // recurrence rule failed to parse (e.g. a legacy row with minified Gson field names) falls
    // back to a different recurrence type than the one its saved builderScheme was written for,
    // which can yield a duplicate-biType list here. Dedupe defensively rather than crash.
    builderItemsHolder.setAll(builderItems.distinctBy { it.biType })
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
