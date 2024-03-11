package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import com.elementary.tasks.core.data.livedata.toSingleEvent
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData

class SubTasksViewModel(
  private val dateTimeManager: DateTimeManager
) {

  private val _showItems = mutableLiveDataOf<List<ShopItem>>()
  val showItems = _showItems.toLiveData()

  private val _saveItems = mutableLiveDataOf<List<ShopItem>>()
  val saveItems = _saveItems.toSingleEvent()

  private var internalItems: List<ShopItem> = emptyList()

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

  fun onTextChanged(position: Int, text: String) {
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
    val newItem = ShopItem(
      createTime = dateTimeManager.getNowGmtDateTime(),
      position = newPosition
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
    val items = internalItems.toMutableList()
    if (position >= items.size) {
      return
    }
    items[position].isChecked = !items[position].isChecked
    items.sortWith(ShopItemComparator())
    items.forEachIndexed { index, shopItem ->
      shopItem.position = index
    }
    postUpdate(items)
  }

  private fun postUpdate(items: List<ShopItem>) {
    this.internalItems = items
    _showItems.postValue(items)
    _saveItems.postValue(items)
  }

  private class ShopItemComparator : Comparator<ShopItem> {

    override fun compare(o1: ShopItem, o2: ShopItem): Int {
      val checkCompare = o1.isChecked.compareTo(o2.isChecked)
      if (checkCompare != 0) {
        return checkCompare
      }
      val dateCompare = o1.createTime.compareTo(o2.createTime)
      if (dateCompare != 0) {
        return dateCompare
      }
      return o1.position.compareTo(o2.position)
    }
  }
}
