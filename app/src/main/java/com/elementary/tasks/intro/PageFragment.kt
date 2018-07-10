package com.elementary.tasks.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.databinding.FragmentIntroPageBinding

import androidx.fragment.app.Fragment

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
class PageFragment : Fragment() {

    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            position = arguments!!.getInt(ARG_POSITION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentIntroPageBinding.inflate(inflater, container, false)
        val item = ItemFactory.getItem(activity, position)
        if (item != null) {
            binding.title.text = item.title
            binding.description.text = item.description
            binding.imageOne.setImageResource(item.images!![0])
            if (item.images!!.size > 1) {
                binding.imageTwo.setImageResource(item.images!![1])
                binding.imageTwo.visibility = View.VISIBLE
            } else {
                binding.imageTwo.visibility = View.INVISIBLE
            }
        }
        return binding.root
    }

    companion object {

        private val ARG_POSITION = "arg_position"

        fun newInstance(position: Int): Fragment {
            val fragment = PageFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}
