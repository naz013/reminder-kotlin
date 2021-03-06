package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.utils.TimeCount
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.FragmentReminderShopBinding
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import timber.log.Timber

class ShopFragment : RepeatableTypeFragment<FragmentReminderShopBinding>() {

  private val mAdapter = ShopListRecyclerAdapter()
  private val mActionListener = object : ShopListRecyclerAdapter.ActionListener {
    override fun onItemCheck(position: Int, isChecked: Boolean) {
      val item = mAdapter.getItem(position)
      item.isChecked = !item.isChecked
      mAdapter.updateData()
      iFace.reminderState.shopItems = mAdapter.data
    }

    override fun onItemDelete(position: Int) {
      mAdapter.delete(position)
      iFace.reminderState.shopItems = mAdapter.data
    }
  }

  override fun prepare(): Reminder? {
    if (mAdapter.itemCount == 0) {
      iFace.showSnackbar(getString(R.string.shopping_list_is_empty))
      return null
    }
    val reminder = iFace.reminderState.reminder
    reminder.shoppings = mAdapter.data
    reminder.target = ""
    reminder.type = Reminder.BY_DATE_SHOP
    reminder.repeatInterval = 0
    reminder.exportToCalendar = false
    reminder.exportToTasks = false
    reminder.hasReminder = binding.attackDelay.isChecked
    reminder.after = 0L
    reminder.dayOfMonth = 0
    reminder.delay = 0
    reminder.eventCount = 0
    if (binding.attackDelay.isChecked) {
      val startTime = binding.dateView.dateTime
      val time = TimeUtil.getGmtFromDateTime(startTime)
      Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
      if (!TimeCount.isCurrent(time)) {
        Toast.makeText(context, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
        return null
      }
      reminder.startTime = time
      reminder.eventTime = time
    } else {
      reminder.eventTime = ""
      reminder.startTime = ""
    }
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderShopBinding.inflate(inflater, container, false)

  override fun provideViews() {
    setViews(
      scrollView = binding.scrollView,
      expansionLayout = binding.moreLayout,
      ledPickerView = binding.ledView,
      extraView = binding.tuneExtraView,
      melodyView = binding.melodyView,
      attachmentView = binding.attachmentView,
      groupView = binding.groupView,
      summaryView = binding.taskSummary,
      dateTimeView = binding.dateView,
      loudnessPickerView = binding.loudnessView,
      priorityPickerView = binding.priorityView,
      windowTypeView = binding.windowTypeView
    )
  }

  override fun onNewHeader(newHeader: String) {
    binding.cardSummary.text = newHeader
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false

    binding.todoList.layoutManager = LinearLayoutManager(context)
    mAdapter.listener = mActionListener
    binding.todoList.adapter = mAdapter
    binding.shopEdit.setOnEditorActionListener { _, actionId, event ->
      if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_NEXT) {
        addNewItem()
        return@setOnEditorActionListener true
      }
      false
    }
    binding.addButton.setOnClickListener { addNewItem() }

    binding.attackDelay.setOnCheckedChangeListener { _, isChecked ->
      iFace.reminderState.isDelayAdded = isChecked
      if (isChecked) {
        binding.delayLayout.visibility = View.VISIBLE
      } else {
        binding.delayLayout.visibility = View.GONE
      }
    }
    binding.delayLayout.visibility = View.GONE
    binding.attackDelay.isChecked = iFace.reminderState.isDelayAdded

    editReminder()
  }

  private fun addNewItem() {
    val task = binding.shopEdit.text.toString().trim()
    if (task == "") {
      binding.shopLayout.error = getString(R.string.must_be_not_empty)
      binding.shopLayout.isErrorEnabled = true
      return
    }
    mAdapter.addItem(ShopItem(task.replace("\n".toRegex(), " ")))
    binding.shopEdit.setText("")
    iFace.reminderState.shopItems = mAdapter.data
  }

  private fun editReminder() {
    val reminder = iFace.reminderState.reminder
    if (!iFace.reminderState.isShopItemsEdited) {
      mAdapter.data = reminder.shoppings
      iFace.reminderState.isShopItemsEdited = true
      iFace.reminderState.shopItems = reminder.shoppings
      binding.attackDelay.isChecked = reminder.hasReminder && !TextUtils.isEmpty(reminder.eventTime)
    } else {
      mAdapter.data = iFace.reminderState.shopItems
    }
  }
}
