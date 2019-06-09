package com.elementary.tasks.core.arch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BindingFragment<B : ViewDataBinding> : Fragment() {

    lateinit var binding: B

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val res = layoutRes()
        val view =  if (res != 0) {
            inflater.inflate(layoutRes(), container, false)
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
        if (view != null) {
            binding = DataBindingUtil.bind(view)!!
        }
        return view
    }

    @LayoutRes
    open fun layoutRes(): Int = 0
}