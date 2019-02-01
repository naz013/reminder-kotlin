package com.elementary.tasks.notes.editor

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
abstract class BitmapFragment<B : ViewDataBinding> : BaseNavigationFragment<B>() {

    protected var editInterface: EditInterface? = null

    override fun onAttach(context: Context?) {
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
