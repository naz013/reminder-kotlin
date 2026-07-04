package com.elementary.tasks.settings.other

import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeWebViewFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class TermsFragment : BaseComposeWebViewFragment() {
  private val viewModel by viewModel<TermsViewModel>()

  override val url: String
    get() = viewModel.url

  override fun getTitle(): String = getString(R.string.terms_and_conditions)
}
