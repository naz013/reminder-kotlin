package com.elementary.tasks.reminder.create.fragments

import androidx.viewbinding.ViewBinding
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.utils.SuperUtil

abstract class RadiusTypeFragment<B : ViewBinding> : TypeFragment<B>() {

  override fun prepare(): Reminder? {
    if (!SuperUtil.checkLocationEnable(requireContext())) {
      SuperUtil.showLocationAlert(requireContext(), iFace)
      return null
    }
    return iFace.state.reminder
  }
}
