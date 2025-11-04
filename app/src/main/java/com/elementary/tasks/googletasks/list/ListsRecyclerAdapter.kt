package com.elementary.tasks.googletasks.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.ListItemGoogleTasksListBinding
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.ui.common.adjustAlpha
import com.github.naz013.ui.common.context.dp2px
import com.github.naz013.ui.common.context.getThemeDimension
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.inflater

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
        createShapeDrawable(
          context = context,
          solidColor = ContextCompat.getColor(context, R.color.md_theme_surface),
          strokeColor = color.adjustAlpha(50)
        )
      )
      stateListDrawable.addState(
        intArrayOf(),
        createShapeDrawable(
          context = context,
          solidColor = ContextCompat.getColor(context, R.color.md_theme_surfaceContainer),
          strokeColor = color
        )
      )
      return stateListDrawable
    }

    /**
     * Creates a shape drawable with stroke color and solid fill color.
     *
     * @param context The context for accessing resources
     * @param solidColor The fill color of the shape
     * @param strokeColor The stroke (border) color of the shape
     * @param strokeWidthDp The stroke width in dp (default is 1dp)
     * @param cornerRadiusDp The corner radius in dp (default is 8dp)
     * @return A GradientDrawable with the specified properties
     */
    private fun createShapeDrawable(
      context: Context,
      solidColor: Int,
      strokeColor: Int,
    ): Drawable {
      return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(solidColor)
        setStroke(context.dp2px(1), strokeColor)
        cornerRadius = context.getThemeDimension(R.attr.cornerRadiusLarge, 0f)
      }
    }
  }
}
