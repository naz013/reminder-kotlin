package com.elementary.tasks.intro

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.databinding.ActivityIntroBinding
import com.elementary.tasks.login.LoginActivity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class IntroActivity : ThemedActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)

        val mPagerAdapter = SlidePagerAdapter(supportFragmentManager)
        binding!!.viewPager.adapter = mPagerAdapter
        binding!!.indicator.setViewPager(binding!!.viewPager)

        binding!!.skipButton.setOnClickListener { v -> moveToNextScreen() }
        binding!!.nextButton.setOnClickListener { v -> moveForward() }
        if (themeUtil!!.isDark) {
            binding!!.nextButton.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp)
        } else {
            binding!!.nextButton.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp)
        }
    }

    private fun moveForward() {
        if (binding!!.viewPager.currentItem >= 4) {
            moveToNextScreen()
        } else {
            binding!!.viewPager.currentItem = binding!!.viewPager.currentItem + 1
        }
    }

    private fun moveToNextScreen() {
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            openLoginScreen()
        } else {
            openFixScreen()
        }
    }

    private fun openFixScreen() {
        startActivity(Intent(this, GoogleFixActivity::class.java))
        finish()
    }

    private fun openLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding!!.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding!!.viewPager.currentItem = binding!!.viewPager.currentItem - 1
        }
    }

    private inner class SlidePagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return PageFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return 5
        }
    }
}
