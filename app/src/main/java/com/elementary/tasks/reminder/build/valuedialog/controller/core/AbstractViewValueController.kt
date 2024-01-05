package com.elementary.tasks.reminder.build.valuedialog.controller.core

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.ValueDialogState
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueController
import com.elementary.tasks.reminder.build.valuedialog.controller.ValueControllerParent

abstract class AbstractViewValueController<T>(
  protected val builderItem: BuilderItem<T>
) : ValueController {

  private val initialValue: T? = builderItem.modifier.getValue()
  protected lateinit var view: View
  private var valueControllerParent: ValueControllerParent? = null

  override fun isDraggable(): Boolean {
    return true
  }

  override fun cancelChanges() {
    builderItem.modifier.update(initialValue)
  }

  override fun clearValue() {
    builderItem.modifier.update(null)
  }

  override fun getItem(): BuilderItem<*> {
    return builderItem
  }

  override fun putValues() { }

  override fun getView(parent: ViewGroup): View {
    view = createView(LayoutInflater.from(parent.context), parent)
    view.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        onViewCreated()
        onDataChanged(builderItem.modifier.getValue())
      }

      override fun onViewDetachedFromWindow(v: View) {
      }
    })
    return view
  }

  override fun onDestroy() { }

  override fun onStop() { }

  override fun onViewAdded(parent: ValueControllerParent) {
    this.valueControllerParent = parent
  }

  open fun onViewCreated() { }

  open fun onDataChanged(data: T?) { }

  override fun onOptionalClicked() {
  }

  abstract fun createView(layoutInflater: LayoutInflater, parent: ViewGroup): View

  protected fun getContext(): Context {
    return view.context
  }

  protected fun updateValue(value: T?) {
    builderItem.modifier.update(value)
    valueControllerParent?.onValueChanged(builderItem)
  }

  protected fun isResumed(): Boolean {
    return valueControllerParent?.getState() == ValueDialogState.RESUMED
  }

  protected fun addOptionalButton(@DrawableRes icon: Int) {
    valueControllerParent?.addOptionalButton(icon)
  }
}
