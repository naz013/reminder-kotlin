package com.elementary.tasks.reminder.create.fragments

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.SuperUtil

abstract class RadiusTypeFragment<B : ViewBinding> : TypeFragment<B>() {

  protected abstract fun recreateMarker()

  override fun prepare(): Reminder? {
    if (!SuperUtil.checkLocationEnable(requireContext())) {
      SuperUtil.showLocationAlert(requireContext(), iFace)
      return null
    }
    return iFace.state.reminder
  }
}
