package com.elementary.tasks.core.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class HolderBinding<B : ViewDataBinding>(
  parent: ViewGroup,
  @LayoutRes res: Int
) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(res, parent, false)) {

  protected var binding: B = DataBindingUtil.bind(itemView)!!
}