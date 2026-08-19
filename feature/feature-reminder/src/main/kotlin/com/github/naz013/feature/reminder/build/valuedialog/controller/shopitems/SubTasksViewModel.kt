package com.github.naz013.feature.reminder.build.valuedialog.controller.shopitems

import androidx.lifecycle.MutableLiveData
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.livedata.toSingleEvent

/** A [ShopItem] paired with its index in the full (unfiltered) item list - carrying that original
 *  index through [SubTasksViewModel.groupedItems]'s active/completed split is what lets callers
 *  keep addressing rows with the existing index-based methods ([onCheckPressed], [onTextChanged],
 *  etc.) after the list has been split into two visual groups. */
data class GroupedShopItems(
  val active: List<IndexedValue<ShopItem>>,
  val completed: List<IndexedValue<ShopItem>>,
  val completedExpanded: Boolean,
)

class SubTasksViewModel(
  private val dateTimeManager: DateTimeManager,
) {
  private val _showItems = MutableLiveData<List<ShopItem>>()
  val showItems = _showItems.toLiveData()

  private val _saveItems = MutableLiveData<List<ShopItem>>()
  val saveItems = _saveItems.toSingleEvent()

  private val _groupedItems = MutableLiveData<GroupedShopItems>()
  val groupedItems = _groupedItems.toLiveData()

  private var internalItems: List<ShopItem> = emptyList()
  private var completedExpanded: Boolean = false

  fun getNonEmptyItems(): List<ShopItem> {
    val items = internalItems
    if (items.size == 1 && items[0].summary.isBlank()) {
      return emptyList()
    }
    return if (items[items.size - 1].summary.isBlank()) {
      items.subList(0, items.size - 1)
    } else {
      items
    }
  }

  fun initWithData(items: List<ShopItem>) {
    if (items.isEmpty()) {
      val newItem = ShopItem(createTime = dateTimeManager.getNowGmtDateTime())
      postUpdate(listOf(newItem))
    } else {
      val mutableList = items.toMutableList()
      mutableList.forEachIndexed { index, shopItem ->
        shopItem.position = index
        shopItem.showInput = false
        shopItem.canRemove = false
      }
      postUpdate(mutableList)
    }
  }

  fun onTextChanged(
    position: Int,
    text: String,
  ) {
    val items = internalItems
    if (position >= items.size) {
      return
    }
    items[position].summary = text
    this.internalItems = items
    _saveItems.postValue(items)
  }

  fun onEnterPressed(position: Int) {
    val items = internalItems.toMutableList()
    if (position >= items.size) {
      return
    }
    val newPosition = position + 1
    val newItem =
      ShopItem(
        createTime = dateTimeManager.getNowGmtDateTime(),
        position = newPosition,
      )
    if (position == items.size - 1) {
      items.add(newItem)
    } else {
      items.add(newPosition, newItem)
    }
    items.forEachIndexed { index, shopItem ->
      shopItem.showInput = false
      shopItem.canRemove = false
      if (index > newPosition) {
        shopItem.position = index
      }
    }
    items[newPosition].showInput = true
    postUpdate(items)
  }

  fun onDeletePressed(position: Int) {
    val items = internalItems.toMutableList()
    if (position >= items.size) {
      return
    }
    if (items.size == 1) {
      return
    }
    items.removeAt(position)
    items.forEachIndexed { index, shopItem ->
      shopItem.position = index
    }
    when {
      position > 0 -> {
        items[position - 1].showInput = true
        items[position - 1].canRemove = items[position - 1].summary.isNotEmpty()
      }
      items.size == 1 -> {
        items[0].showInput = true
        items[0].canRemove = items[0].summary.isNotEmpty()
      }
    }
    postUpdate(items)
  }

  fun onRemovePressed(position: Int) {
    val items = internalItems.toMutableList()
    if (position >= items.size) {
      return
    }
    val removed = items.removeAt(position)
    if (items.isEmpty()) {
      val newItem = ShopItem(createTime = dateTimeManager.getNowGmtDateTime())
      items.add(newItem)
    }
    items.forEachIndexed { index, shopItem ->
      shopItem.position = index
    }
    if (removed.showInput) {
      items[items.size - 1].showInput = true
      items[items.size - 1].canRemove = items[items.size - 1].summary.isNotEmpty()
    }
    postUpdate(items)
  }

  fun onCheckPressed(position: Int) {
    val items = internalItems
    if (position >= items.size) {
      return
    }
    val updated = items.toMutableList()
    updated[position] = updated[position].copy(isChecked = !updated[position].isChecked)
    postUpdate(updated)
  }

  fun onCompletedToggle() {
    completedExpanded = !completedExpanded
    _groupedItems.postValue(toGroupedItems(internalItems))
  }

  /** Reorders an active (unchecked) item within the full list - [fromIndex]/[toIndex] are indices
   *  into the full list, same convention as [onCheckPressed] and friends, not indices into
   *  [GroupedShopItems.active]. `position` isn't persisted (see [ShopItem]), so reassigning it here
   *  is only to keep it consistent with the other mutators - actual save order comes from the list
   *  itself. */
  fun onReorder(
    fromIndex: Int,
    toIndex: Int,
  ) {
    val items = internalItems
    if (fromIndex == toIndex || fromIndex !in items.indices || toIndex !in items.indices) {
      return
    }
    val updated = items.toMutableList()
    val moved = updated.removeAt(fromIndex)
    updated.add(toIndex, moved)
    updated.forEachIndexed { index, shopItem -> shopItem.position = index }
    postUpdate(updated)
  }

  private fun toGroupedItems(items: List<ShopItem>): GroupedShopItems {
    val (completed, active) = items.withIndex().partition { it.value.isChecked }
    return GroupedShopItems(active = active, completed = completed, completedExpanded = completedExpanded)
  }

  private fun postUpdate(items: List<ShopItem>) {
    this.internalItems = items
    _showItems.postValue(items)
    _saveItems.postValue(items)
    _groupedItems.postValue(toGroupedItems(items))
  }
}
