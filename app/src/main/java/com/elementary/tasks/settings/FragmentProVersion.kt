package com.elementary.tasks.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.databinding.FragmentSettingsProPageBinding

class FragmentProVersion : BaseSettingsFragment<FragmentSettingsProPageBinding>() {

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsProPageBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.buttonBuy.setOnClickListener { openMarket() }
  }

  private fun openMarket() {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("market://details?id=" + "com.cray.software.justreminderpro")
    try {
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(context, R.string.could_not_launch_market, Toast.LENGTH_SHORT).show()
    }
  }

  override fun getTitle(): String = getString(R.string.pro_version)
}
