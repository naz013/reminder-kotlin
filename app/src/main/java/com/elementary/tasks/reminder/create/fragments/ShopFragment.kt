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
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.visibleGone
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
      iFace.state.shopItems = mAdapter.data
    }

    override fun onItemDelete(position: Int) {
      mAdapter.delete(position)
      iFace.state.shopItems = mAdapter.data
    }
  }

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.SHOPPING
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    if (mAdapter.itemCount == 0) {
      iFace.showSnackbar(getString(R.string.shopping_list_is_empty))
      return null
    }
    val reminder = iFace.state.reminder
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
    reminder.recurDataObject = null

    if (binding.attackDelay.isChecked) {
      val startTime = binding.dateView.selectedDateTime
      val time = dateTimeManager.getGmtFromDateTime(startTime)
      Timber.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))
      if (!dateTimeManager.isCurrent(time)) {
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

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.tuneExtraView,
      binding.melodyView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.dateView,
      binding.loudnessView,
      binding.priorityView,
      binding.windowTypeView
    )
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
      iFace.state.isDelayAdded = isChecked
      binding.dateView.visibleGone(isChecked)
    }
    binding.dateView.gone()
    binding.attackDelay.isChecked = iFace.state.isDelayAdded

    editReminder()
  }

  private fun addNewItem() {
    val task = binding.shopEdit.text.toString().trim()
    if (task == "") {
      binding.shopLayout.error = getString(R.string.must_be_not_empty)
      binding.shopLayout.isErrorEnabled = true
      return
    }
    mAdapter.addItem(
      ShopItem(
        task.replace("\n".toRegex(), " "),
        createTime = dateTimeManager.getNowGmtDateTime()
      )
    )
    binding.shopEdit.setText("")
    iFace.state.shopItems = mAdapter.data
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    if (!iFace.state.isShopItemsEdited) {
      mAdapter.data = reminder.shoppings
      iFace.state.isShopItemsEdited = true
      iFace.state.shopItems = reminder.shoppings
      binding.attackDelay.isChecked = reminder.hasReminder && !TextUtils.isEmpty(reminder.eventTime)
    } else {
      mAdapter.data = iFace.state.shopItems
    }
  }
}
