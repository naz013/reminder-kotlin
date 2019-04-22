package com.elementary.tasks.notes.editor

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

abstract class BitmapFragment<B : ViewDataBinding> : BaseNavigationFragment<B>() {

    protected var editInterface: EditInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val ei = editInterface
        if (ei == null) {
            editInterface = context as EditInterface?
        }
    }

    abstract val image: ByteArray?

    abstract val originalImage: ByteArray?

    abstract fun onBackPressed(): Boolean
}
