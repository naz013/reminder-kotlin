package com.elementary.tasks.googletasks.list

import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.github.naz013.domain.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.view.inflater
import com.elementary.tasks.databinding.ListItemGoogleTasksListBinding

class ListsRecyclerAdapter :
  ListAdapter<GoogleTaskList, ListsRecyclerAdapter.Holder>(GoogleTasksListDiffCallback()) {

  var actionsListener: ActionsListener<GoogleTaskList>? = null

  override fun onBindViewHolder(holder: Holder, position: Int) {
    holder.bind(getItem(position))
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(parent)

  inner class Holder(
    parent: ViewGroup
  ) : HolderBinding<ListItemGoogleTasksListBinding>(
    ListItemGoogleTasksListBinding.inflate(parent.inflater(), parent, false)
  ) {

    init {
      binding.clickView.setOnClickListener {
        actionsListener?.onAction(
          it,
          bindingAdapterPosition,
          getItem(bindingAdapterPosition),
          ListActions.OPEN
        )
      }
    }

    fun bind(googleTaskList: GoogleTaskList) {
      binding.textView.text = googleTaskList.title
      val color = ThemeProvider.themedColor(binding.textView.context, googleTaskList.color)
      binding.clickView.background = stateList(color)
    }

    private fun stateList(color: Int): StateListDrawable {
      val context = binding.clickView.context
      val stateListDrawable = StateListDrawable()
      stateListDrawable.addState(
        intArrayOf(android.R.attr.state_pressed),
        tint(
          ContextCompat.getDrawable(context, R.drawable.gradient_button_google_pressed),
          color.adjustAlpha(75)
        )
      )
      stateListDrawable.addState(
        intArrayOf(),
        tint(
          ContextCompat.getDrawable(context, R.drawable.gradient_button_google_pressed),
          color
        )
      )
      return stateListDrawable
    }

    private fun tint(drawable: Drawable?, color: Int): Drawable? {
      drawable?.setTint(color)
      return drawable
    }
  }
}
