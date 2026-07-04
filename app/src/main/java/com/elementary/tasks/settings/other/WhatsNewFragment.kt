package com.elementary.tasks.settings.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWhatsNewBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewFragment : BaseSettingsFragment<FragmentSettingsWhatsNewBinding>() {
  private val viewModel by viewModel<WhatsNewViewModel>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ) = FragmentSettingsWhatsNewBinding.inflate(inflater, container, false)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    binding.dateAndVersionView.text =
      getString(
        R.string.whats_new_version_and_date,
        viewModel.versionName,
        viewModel.buildDate,
      )
  }

  override fun getTitle(): String = getString(R.string.whats_new)
}
