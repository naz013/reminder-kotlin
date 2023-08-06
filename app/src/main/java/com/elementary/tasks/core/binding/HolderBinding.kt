package com.elementary.tasks.core.binding

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class HolderBinding<B : ViewBinding>(
  protected val binding: B
) : RecyclerView.ViewHolder(binding.root)
