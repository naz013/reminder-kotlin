package com.elementary.tasks.reminder.build.valuedialog.controller.shopitems

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.github.naz013.domain.reminder.ShopItem
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.github.naz013.feature.common.android.transparent
import com.elementary.tasks.core.utils.ui.trimmedText
import com.github.naz013.feature.common.android.visibleInvisible
import com.elementary.tasks.databinding.ListItemReminderBuilderShopItemBinding

class ShopItemViewHolder(
  parent: ViewGroup,
  private val inputMethodManager: InputMethodManager,
  private val onTextChanged: (Int, String) -> Unit,
  private val onCheckClicked: (Int) -> Unit,
  private val onRemoveClicked: (Int) -> Unit,
  private val onEnterPressed: (Int) -> Unit,
  private val onDeletePressed: (Int) -> Unit,
  private val binding: ListItemReminderBuilderShopItemBinding =
    ListItemReminderBuilderShopItemBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
) : RecyclerView.ViewHolder(binding.root) {

  private var textWatcher: (String?) -> Unit = { }

  init {
    binding.itemCheckView.setOnClickListener {
      onCheckClicked(bindingAdapterPosition)
    }
    binding.removeButton.setOnClickListener {
      onRemoveClicked(bindingAdapterPosition)
    }
    binding.removeButton.transparent()

    binding.textInputView.onTextChanged { text ->
      textWatcher(text)
    }
  }

  fun bind(item: ShopItem) {
    binding.textInputView.setText(item.summary)

    textWatcher = { text ->
      val nonNullText = text ?: ""
      if (nonNullText != item.summary) {
        binding.removeButton.visibleInvisible(nonNullText.isNotEmpty())
        if (nonNullText.endsWith("\n")) {
          if (nonNullText.length == 1) {
            binding.textInputView.setText("")
            binding.removeButton.transparent()
          } else {
            onEnterPressed(bindingAdapterPosition)
          }
        } else {
          onTextChanged(bindingAdapterPosition, nonNullText)
        }
      }
    }

    binding.textInputView.setOnKeyListener { v, keyCode, event ->
      return@setOnKeyListener if ((event.action == KeyEvent.ACTION_DOWN) &&
        (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
      ) {
        if (!binding.textInputView.text.isNullOrEmpty()) {
          onEnterPressed(bindingAdapterPosition)
        }
        true
      } else if ((event.action == KeyEvent.ACTION_DOWN) && keyCode == KeyEvent.KEYCODE_DEL) {
        if (binding.textInputView.text.isNullOrEmpty()) {
          onDeletePressed(bindingAdapterPosition)
          true
        } else {
          false
        }
      } else {
        false
      }
    }
    binding.textInputView.setOnFocusChangeListener { _, hasFocus ->
      binding.removeButton.visibleInvisible(
        hasFocus && binding.textInputView.trimmedText().isNotEmpty()
      )
    }

    if (item.isChecked) {
      binding.itemCheckView.setImageResource(R.drawable.ic_fluent_checkbox_checked)
    } else {
      binding.itemCheckView.setImageResource(R.drawable.ic_fluent_checkbox_unchecked)
    }
    binding.removeButton.visibleInvisible(item.canRemove)
    if (item.showInput) {
      binding.textInputView.requestFocus()
      binding.textInputView.setSelection(binding.textInputView.text.toString().length)
      inputMethodManager.showSoftInput(binding.textInputView, InputMethodManager.SHOW_FORCED)
    }
  }
}
