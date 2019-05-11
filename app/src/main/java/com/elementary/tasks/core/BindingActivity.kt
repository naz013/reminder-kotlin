package com.elementary.tasks.core

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.utils.activityBinding

abstract class BindingActivity<B : ViewDataBinding>(@LayoutRes layoutRes: Int) : ThemedActivity() {

    protected val binding: B by activityBinding(layoutRes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
    }
}
