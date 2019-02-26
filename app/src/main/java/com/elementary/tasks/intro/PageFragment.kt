package com.elementary.tasks.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.BindingFragment
import com.elementary.tasks.databinding.FragmentIntroPageBinding

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
class PageFragment : BindingFragment<FragmentIntroPageBinding>() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            position = arguments?.getInt(ARG_POSITION) ?: 0
        }
    }

    override fun layoutRes(): Int = R.layout.fragment_intro_page

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = ItemFactory.getItem(activity!!, position)
        if (item != null) {
            binding.animationView.setAnimation(item.image)
            binding.animationView.playAnimation()
        }
    }

    companion object {

        private const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int): Fragment {
            val fragment = PageFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
