package com.elementary.tasks.navigation

import androidx.recyclerview.widget.RecyclerView

interface SearchableFragmentCallback {
  fun setQuery(text: String)
  fun setSearchViewParams(
    anchorId: Int,
    hint: String,
    adapter: RecyclerView.Adapter<*>,
    observer: SearchableFragmentQueryObserver
  )
  fun removeSearchView()
}

interface SearchableFragmentQueryObserver {
  fun onQueryChanged(text: String)
}
