package com.elementary.tasks.settings.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.databinding.FragmentSettingsThemePreviewBinding
import com.elementary.tasks.settings.BaseSettingsFragment

class UiPreviewFragment : BaseSettingsFragment<FragmentSettingsThemePreviewBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsThemePreviewBinding.inflate(inflater, container, false)

  override fun getTitle(): String = "Theme Preview"
}
