package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.elementary.tasks.databinding.BuilderItemShopItemsBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class SubTasksController(
  builderItem: BuilderItem<List<ShopItem>>,
  private val viewModel: SubTasksViewModel,
  private val viewLifecycleOwner: LifecycleOwner,
  inputMethodManager: InputMethodManager
) : AbstractBindingValueController<List<ShopItem>, BuilderItemShopItemsBinding>(builderItem) {

  private val shopItemsAdapter = ShopItemsAdapter(
    inputMethodManager = inputMethodManager,
    onCheckClicked = { position ->
      viewModel.onCheckPressed(position)
    },
    onEnterPressed = { position ->
      viewModel.onEnterPressed(position)
    },
    onRemoveClicked = { position ->
      viewModel.onRemovePressed(position)
    },
    onTextChanged = { position, s ->
      viewModel.onTextChanged(position, s)
    },
    onDeletePressed = { position ->
      viewModel.onDeletePressed(position)
    }
  )

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemShopItemsBinding {
    return BuilderItemShopItemsBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.itemsListView.layoutManager = LinearLayoutManager(getContext())
    binding.itemsListView.adapter = shopItemsAdapter

    viewModel.showItems.nonNullObserve(viewLifecycleOwner) { shopItemsAdapter.submitList(it) }
    viewModel.saveItems.nonNullObserve(viewLifecycleOwner) { updateValue(it) }
  }

  override fun onDataChanged(data: List<ShopItem>?) {
    super.onDataChanged(data)
    viewModel.initWithData(data ?: emptyList())
  }

  override fun putValues() {
    super.putValues()
    builderItem.modifier.update(viewModel.getNonEmptyItems())
  }

  override fun onDestroy() {
    super.onDestroy()
    viewModel.showItems.removeObservers(viewLifecycleOwner)
  }
}
