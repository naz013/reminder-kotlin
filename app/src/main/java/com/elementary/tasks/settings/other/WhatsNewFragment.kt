package com.elementary.tasks.settings.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsWhatsNewBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class WhatsNewFragment : BaseSettingsFragment<FragmentSettingsWhatsNewBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsWhatsNewBinding.inflate(inflater, container, false)

  override fun getTitle(): String = getString(R.string.whats_new)
}
