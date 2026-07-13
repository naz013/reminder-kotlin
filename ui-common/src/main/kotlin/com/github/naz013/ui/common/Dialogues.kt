package com.github.naz013.ui.common

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.github.naz013.ui.common.context.dp2px
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@Deprecated("Use Compose dialogs")
class Dialogues {

  fun getMaterialDialog(context: Context): MaterialAlertDialogBuilder {
    return MaterialAlertDialogBuilder(context)
  }

  fun askConfirmation(
    context: Context,
    title: String,
    message: String = context.getString(R.string.are_you_sure),
    positiveText: String = context.getString(R.string.yes),
    negativeText: String = context.getString(R.string.no),
    onAction: (Boolean) -> Unit
  ) {
    getMaterialDialog(context)
      .setTitle(title)
      .setMessage(message)
      .setPositiveButton(positiveText) { dialog, _ ->
        dialog.dismiss()
        onAction.invoke(true)
      }
      .setNegativeButton(negativeText) { dialog, _ ->
        dialog.dismiss()
        onAction.invoke(false)
      }
      .create()
      .show()
  }

  companion object {

    fun showPopup(
      anchor: View,
      listener: ((Int) -> Unit)?,
      vararg actions: String
    ) {
      val popupMenu = PopupMenu(anchor.context, anchor)
      popupMenu.setOnMenuItemClickListener { item ->
        listener?.invoke(item.order)
        true
      }
      for (i in actions.indices) {
        popupMenu.menu.add(1, i + 1000, i, actions[i])
      }
      popupMenu.show()
    }

    fun setFullWidthDialog(dialog: AlertDialog, activity: Activity?) {
      if (activity == null) return
      val window = dialog.window
      window?.setGravity(Gravity.CENTER)
      window?.setLayout((activity.dp2px(380)), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
  }
}
