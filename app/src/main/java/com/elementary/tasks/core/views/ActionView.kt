package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.ActionViewBinding
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.hide
import com.elementary.tasks.core.utils.show

class ActionView : LinearLayout, TextWatcher {

  private var mImm: InputMethodManager? = null
  private var listener: OnActionListener? = null
  private var permissionFlow: PermissionFlow? = null
  private lateinit var binding: ActionViewBinding

  var type: Int
    get() = if (hasAction()) {
      if (binding.callAction.isChecked) {
        TYPE_CALL
      } else {
        TYPE_MESSAGE
      }
    } else {
      0
    }
    set(type) = if (type == TYPE_CALL) {
      binding.callAction.isChecked = true
    } else {
      binding.messageAction.isChecked = true
    }

  var number: String
    get() = binding.numberView.text.toString().trim()
    set(number) = binding.numberView.setText(number)

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_action, this)
    orientation = VERTICAL
    binding = ActionViewBinding(this)

    binding.actionBlock.hide()

    binding.numberView.isFocusableInTouchMode = true
    binding.numberView.setOnFocusChangeListener { _, hasFocus ->
      mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      if (!hasFocus) {
        mImm?.hideSoftInputFromWindow(binding.numberView.windowToken, 0)
      } else {
        mImm?.showSoftInput(binding.numberView, 0)
      }
    }
    binding.numberView.setOnClickListener {
      mImm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
      if (mImm?.isActive(binding.numberView) == false) {
        mImm?.showSoftInput(binding.numberView, 0)
      }
    }
    binding.numberView.addTextChangedListener(this)
    binding.radioGroup.setOnCheckedChangeListener { _, i -> buttonClick(i) }
    binding.callAction.isChecked = true
    binding.actionCheck.setOnCheckedChangeListener { _, b ->
      if (b) {
        permissionFlow?.askPermission(Permissions.READ_CONTACTS) {
          openAction()
        } ?: run {
          binding.actionCheck.isChecked = false
        }
      } else {
        binding.actionBlock.hide()
      }
      listener?.onStateChanged(hasAction(), type, number)
    }
    if (binding.actionCheck.isChecked) {
      openAction()
    }
  }

  private fun openAction() {
    binding.actionBlock.show()
    refreshState()
  }

  private fun refreshState() {
    buttonClick(binding.radioGroup.checkedRadioButtonId)
  }

  private fun buttonClick(i: Int) {
    when (i) {
      R.id.callAction -> listener?.onStateChanged(hasAction(), type, number)
      R.id.messageAction -> listener?.onStateChanged(hasAction(), type, number)
    }
  }

  fun setPermissionHandle(permissionFlow: PermissionFlow) {
    this.permissionFlow = permissionFlow
  }

  fun setContactClickListener(contactClickListener: OnClickListener) {
    binding.selectNumber.setOnClickListener(contactClickListener)
  }

  fun setListener(listener: OnActionListener) {
    this.listener = listener
  }

  fun hasAction(): Boolean {
    return binding.actionCheck.isChecked
  }

  fun setAction(action: Boolean) {
    binding.actionCheck.isChecked = action
  }

  override fun afterTextChanged(s: Editable?) {
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    listener?.onStateChanged(hasAction(), type, number)
  }

  interface OnActionListener {
    fun onStateChanged(hasAction: Boolean, type: Int, phone: String)
  }

  companion object {
    const val TYPE_CALL = 1
    const val TYPE_MESSAGE = 2
  }
}
