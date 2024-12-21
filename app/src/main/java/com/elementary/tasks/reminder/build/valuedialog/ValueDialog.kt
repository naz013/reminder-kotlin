package com.elementary.tasks.reminder.build.valuedialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.naz013.feature.common.android.gone
import com.github.naz013.feature.common.android.visible
import com.elementary.tasks.databinding.BottomSheetValueSelectorBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueController
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueControllerFactory
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueControllerParent
import com.github.naz013.logging.Logger
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject

class ValueDialog : BottomSheetDialogFragment(), ParentDialogHandle, ValueControllerParent {

  private val dataHolder by inject<ValueDialogDataHolder>()
  private val controllerFactory by inject<ValueControllerFactory>()

  private lateinit var binding: BottomSheetValueSelectorBinding
  private var callback: ValueDialogCallback? = null
  private var controller: ValueController? = null
  private var lifecycleDispatcher: ValueDialogLifecycleDispatcher? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    runCatching {
      callback = context as? ValueDialogCallback
    }
  }

  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)
    save()
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    save()
  }

  override fun onStop() {
    lifecycleDispatcher?.dispatchOnStop()
    super.onStop()
  }

  override fun onDetach() {
    super.onDetach()
    lifecycleDispatcher?.dispatchOnDestroy()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = BottomSheetValueSelectorBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.dialogCloseButton.setOnClickListener { dismiss() }
    binding.optionalButton.setOnClickListener { controller?.onOptionalClicked() }
    binding.optionalButton.gone()

    binding.clearButton.setOnClickListener {
      controller?.clearValue()
      save()
      dismiss()
    }

    binding.saveButton.setOnClickListener {
      controller?.putValues()
      save()
      dismiss()
    }
    binding.buttonsHolder.gone()

    Logger.d("onViewCreated: position=${getPosition()}")
    dataHolder.data?.also { builderItem ->
      binding.titleView.text = builderItem.title
      binding.descriptionView.text = builderItem.description

      binding.viewContainer.removeAllViewsInLayout()
      val controller = controllerFactory.create(this, builderItem).also {
        this.controller = it
      }
      lifecycleDispatcher = ValueDialogLifecycleDispatcher(controller)
      lifecycleDispatcher?.dispatchOnCreate()

      binding.viewContainer.addView(controller.getView(binding.viewContainer))
      controller.onViewAdded(this)
      lifecycleDispatcher?.dispatchOnResume()
    }

    updateBehavior()
  }

  private fun save() {
    controller?.getItem()?.also {
      callback?.onValueChanged(getPosition(), it)
    }
  }

  private fun getPosition(): Int {
    return arguments?.getInt(KEY_POSITION) ?: 0
  }

  override fun updateBehavior() {
    val dialog = dialog as? BottomSheetDialog
    dialog?.behavior?.isDraggable = controller?.isDraggable() ?: true
  }

  override fun onValueChanged(builderItem: BuilderItem<*>) {
    callback?.onValueChanged(getPosition(), builderItem)
  }

  override fun getState(): ValueDialogState {
    return lifecycleDispatcher?.state ?: ValueDialogState.NONE
  }

  override fun addOptionalButton(icon: Int) {
    binding.optionalButton.setImageResource(icon)
    binding.optionalButton.visible()
  }

  companion object {
    const val TAG = "ValueDialog"
    private const val KEY_POSITION = "arg_position"

    @JvmStatic
    fun newInstance(position: Int): ValueDialog {
      return ValueDialog().apply {
        arguments = Bundle().apply {
          putInt(KEY_POSITION, position)
        }
      }
    }
  }
}
