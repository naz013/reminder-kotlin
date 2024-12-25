package com.elementary.tasks.globalsearch.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.common.view.transparent
import com.github.naz013.ui.common.view.visible
import com.elementary.tasks.databinding.ListItemSearchResultBinding
import com.elementary.tasks.globalsearch.ObjectSearchResult
import com.elementary.tasks.globalsearch.ObjectType
import com.elementary.tasks.globalsearch.RecentObjectSearchResult
import com.elementary.tasks.globalsearch.RecentSearchResult
import com.elementary.tasks.globalsearch.SearchResult

abstract class SearchViewHolder<T : SearchResult>(
  parent: ViewGroup,
  private val clickListener: (position: Int, action: ClickAction) -> Unit,
  protected val binding: ListItemSearchResultBinding = ListItemSearchResultBinding.inflate(
    LayoutInflater.from(parent.context),
    parent,
    false
  )
) : RecyclerView.ViewHolder(binding.root) {

  init {
    binding.clickView.setOnClickListener {
      clickListener(bindingAdapterPosition, ClickAction.NORMAL)
    }
    binding.endIconView.setOnClickListener {
      clickListener(bindingAdapterPosition, ClickAction.INSERT)
    }
  }

  abstract fun bind(result: T)

  protected fun bindText(text: String, query: String) {
    val spannable: Spannable = SpannableString(text)
    val startIndex = text.lowercase().indexOf(query.lowercase())
    if (startIndex != -1) {
      spannable.setSpan(
        BackgroundColorSpan(ThemeProvider.getSecondaryContainerColor(binding.root.context)),
        startIndex,
        startIndex + query.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    binding.textView.text = spannable
  }
}

class RecentSearchViewHolder(
  parent: ViewGroup,
  clickListener: (position: Int, action: ClickAction) -> Unit
) : SearchViewHolder<RecentSearchResult>(parent, clickListener) {

  init {
    binding.endIconView.visible()
  }

  override fun bind(result: RecentSearchResult) {
    binding.textView.text = result.text
    binding.startIconView.setImageResource(R.drawable.ic_fluent_history)
    binding.endIconView.setImageResource(R.drawable.ic_fluent_arrow_reply)
  }

  companion object {
    const val VIEW_TYPE = 1
  }
}

class RecentObjectSearchViewHolder(
  parent: ViewGroup,
  clickListener: (position: Int, action: ClickAction) -> Unit
) : SearchViewHolder<RecentObjectSearchResult>(parent, clickListener) {

  init {
    binding.endIconView.visible()
  }

  override fun bind(result: RecentObjectSearchResult) {
    binding.textView.text = result.text
    binding.startIconView.setImageResource(R.drawable.ic_fluent_history)
    binding.endIconView.setImageResource(R.drawable.ic_fluent_arrow_reply)
  }

  companion object {
    const val VIEW_TYPE = 2
  }
}

class ObjectSearchViewHolder(
  parent: ViewGroup,
  clickListener: (position: Int, action: ClickAction) -> Unit
) : SearchViewHolder<ObjectSearchResult>(parent, clickListener) {

  init {
    binding.endIconView.transparent()
  }

  override fun bind(result: ObjectSearchResult) {
    bindText(result.text, result.query)
    binding.startIconView.setImageResource(getStartIcon(result.objectType))
    binding.endIconView.setImageResource(0)
  }

  private fun getStartIcon(objectType: ObjectType): Int {
    return when (objectType) {
      ObjectType.REMINDER -> R.drawable.ic_fluent_alert
      ObjectType.BIRTHDAY -> R.drawable.ic_fluent_food_cake
      ObjectType.GOOGLE_TASK -> R.drawable.ic_builder_google_task_list
      ObjectType.GROUP -> R.drawable.ic_builder_group
      ObjectType.NOTE -> R.drawable.ic_fluent_note
      ObjectType.PLACE -> R.drawable.ic_fluent_place
    }
  }

  companion object {
    const val VIEW_TYPE = 3
  }
}

enum class ClickAction {
  NORMAL, INSERT
}
