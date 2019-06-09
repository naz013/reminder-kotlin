package com.elementary.tasks.intro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.databinding.FragmentIntroPageBinding

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
