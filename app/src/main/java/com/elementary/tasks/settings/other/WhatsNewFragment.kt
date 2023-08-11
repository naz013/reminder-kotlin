package com.elementary.tasks.settings.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.databinding.FragmentSettingsWhatsNewBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject

class WhatsNewFragment : BaseSettingsFragment<FragmentSettingsWhatsNewBinding>() {

  private val packageManagerWrapper by inject<PackageManagerWrapper>()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsWhatsNewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    updateDateAndVersion()
  }

  private fun updateDateAndVersion() {
    val versionName = packageManagerWrapper.getVersionName()
    val date = StringUtils.capitalize(BuildConfig.BUILD_DATE)

    binding.dateAndVersionView.text = getString(
      R.string.whats_new_version_and_date,
      versionName,
      date
    )
  }

  override fun getTitle(): String = getString(R.string.whats_new)
}
