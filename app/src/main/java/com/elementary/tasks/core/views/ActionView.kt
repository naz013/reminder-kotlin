package com.elementary.tasks.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.github.naz013.common.Permissions
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.ViewActionBinding
import com.github.naz013.logging.Logger

class ActionView : LinearLayout, TextWatcher {

  private var mImm: InputMethodManager? = null
  private var listener: OnActionListener? = null
  private var permissionFlow: PermissionFlow? = null
  private lateinit var binding: ViewActionBinding
  private var internalState: ActionState = ActionState.NO_ACTION

  var actionState: ActionState
    get() = internalState
    set(value) {
      selectButton(value)
      setState(value)
    }

  var number: String
    get() = binding.numberView.trimmedText()
    set(number) = binding.numberView.setText(number)

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int
  ) : super(context, attrs, defStyle) {
    init(context)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_action, this)
    orientation = VERTICAL
    binding = ViewActionBinding.bind(this)

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
    binding.actionGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      if (isChecked) {
        if (checkedId == R.id.noAction) {
          setState(ActionState.NO_ACTION)
        } else {
          val state = if (checkedId == R.id.callAction) {
            ActionState.CALL
          } else {
            ActionState.SMS
          }
          permissionFlow?.askPermission(Permissions.READ_CONTACTS) {
            setState(state)
          } ?: run {
            selectButton(ActionState.NO_ACTION)
          }
        }
      }
    }
    selectButton(ActionState.NO_ACTION)
    setState(ActionState.NO_ACTION)
  }

  private fun selectButton(state: ActionState) {
    val buttonId = when (state) {
      ActionState.NO_ACTION -> R.id.noAction
      ActionState.CALL -> R.id.callAction
      ActionState.SMS -> R.id.smsAction
    }
    binding.actionGroup.check(buttonId)
  }

  private fun setState(state: ActionState) {
    Logger.d("setState: $state")
    this.internalState = state
    enableViews(state != ActionState.NO_ACTION)
    listener?.onStateChanged(state, number)
  }

  private fun enableViews(isEnabled: Boolean) {
    binding.numberLayout.isEnabled = isEnabled
    binding.selectNumber.isEnabled = isEnabled
    binding.numberView.isEnabled = isEnabled
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
    return internalState != ActionState.NO_ACTION
  }

  override fun afterTextChanged(s: Editable?) {
  }

  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
  }

  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    listener?.onStateChanged(internalState, number)
  }

  interface OnActionListener {
    fun onStateChanged(state: ActionState, phone: String)
  }

  enum class ActionState(val value: Int) {
    NO_ACTION(0), CALL(1), SMS(2)
  }
}
