package com.elementary.tasks.reminder.build.valuedialog.controller.action

import android.view.LayoutInflater
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.common.PackageManagerWrapper
import com.elementary.tasks.core.os.datapicker.ApplicationPicker
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import com.elementary.tasks.databinding.BuilderItemApplicationBinding
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.core.AbstractBindingValueController

class ApplicationController(
  builderItem: BuilderItem<String>,
  private val applicationPicker: ApplicationPicker,
  private val packageManagerWrapper: PackageManagerWrapper
) : AbstractBindingValueController<String, BuilderItemApplicationBinding>(builderItem) {

  override fun bindView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup
  ): BuilderItemApplicationBinding {
    return BuilderItemApplicationBinding.inflate(layoutInflater, parent, false)
  }

  override fun onViewCreated() {
    super.onViewCreated()
    binding.pickApplicationButton.setOnClickListener {
      applicationPicker.pickApplication {
        builderItem.modifier.update(it)
        onDataChanged(it)
      }
    }
  }

  override fun onDataChanged(data: String?) {
    super.onDataChanged(data)
    data?.let { packageManagerWrapper.getAppInfo(it) }
      ?.also {
        binding.applicationNameView.text =
          it.loadLabel(packageManagerWrapper.packageManager).toString()
        val drawable = it.loadIcon(packageManagerWrapper.packageManager)
        if (drawable != null) {
          binding.applicationIconView.setImageDrawable(drawable)
          binding.applicationIconView.visible()
        } else {
          binding.applicationIconView.gone()
        }
      }
      ?: run {
        binding.applicationNameView.text = getContext().getString(R.string.application_not_selected)
        binding.applicationIconView.gone()
      }
  }
}
