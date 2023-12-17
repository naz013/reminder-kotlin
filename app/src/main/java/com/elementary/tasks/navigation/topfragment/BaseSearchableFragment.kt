package com.elementary.tasks.navigation.topfragment

import android.content.Context
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.navigation.SearchableFragmentCallback
import com.elementary.tasks.navigation.SearchableFragmentQueryObserver

abstract class BaseSearchableFragment<B : ViewBinding> :
  BaseTopFragment<B>(),
  SearchableFragmentQueryObserver {

  protected var searchableFragmentCallback: SearchableFragmentCallback? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (searchableFragmentCallback == null) {
      runCatching { searchableFragmentCallback = context as SearchableFragmentCallback? }
    }
  }
}
