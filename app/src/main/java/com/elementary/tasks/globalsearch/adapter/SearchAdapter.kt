package com.elementary.tasks.globalsearch.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.globalsearch.ObjectSearchResult
import com.elementary.tasks.globalsearch.RecentObjectSearchResult
import com.elementary.tasks.globalsearch.RecentSearchResult
import com.elementary.tasks.globalsearch.SearchResult

class SearchAdapter(
  private val onSuggestionClick: (SearchResult, String) -> Unit,
  private val onObjectClick: (SearchResult) -> Unit
) : ListAdapter<SearchResult, SearchViewHolder<*>>(
  SearchResultDiffCallback()
) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): SearchViewHolder<*> {
    val clickListener: (Int, ClickAction) -> Unit = { position, action ->
      when (val item = getItem(position)) {
        is RecentObjectSearchResult -> {
          if (action == ClickAction.INSERT) {
            onSuggestionClick(item, item.text)
          } else {
            onObjectClick(item)
          }
        }
        is ObjectSearchResult -> {
          onObjectClick(item)
        }
        is RecentSearchResult -> {
          onSuggestionClick(item, item.text)
        }
      }
    }
    return when (viewType) {
      RecentObjectSearchViewHolder.VIEW_TYPE -> {
        RecentObjectSearchViewHolder(parent, clickListener)
      }
      ObjectSearchViewHolder.VIEW_TYPE -> {
        ObjectSearchViewHolder(parent, clickListener)
      }
      else -> {
        RecentSearchViewHolder(parent, clickListener)
      }
    }
  }

  override fun onBindViewHolder(holder: SearchViewHolder<*>, position: Int) {
    when (holder) {
      is RecentObjectSearchViewHolder -> {
        holder.bind(getItem(position) as RecentObjectSearchResult)
      }
      is ObjectSearchViewHolder -> {
        holder.bind(getItem(position) as ObjectSearchResult)
      }
      is RecentSearchViewHolder -> {
        holder.bind(getItem(position) as RecentSearchResult)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is RecentSearchResult -> RecentSearchViewHolder.VIEW_TYPE
      is RecentObjectSearchResult -> RecentObjectSearchViewHolder.VIEW_TYPE
      is ObjectSearchResult -> ObjectSearchViewHolder.VIEW_TYPE
    }
  }
}

class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResult>() {
  override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
    return oldItem.query == newItem.query
  }

  override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean {
    return oldItem == newItem
  }
}
