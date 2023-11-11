package com.elementary.tasks.home.scheduleview.viewholder

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.core.binding.HolderBinding

abstract class BaseScheduleHolder<B : ViewBinding>(
  binding: B
) : HolderBinding<B>(binding)
