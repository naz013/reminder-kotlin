package com.elementary.tasks.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.login.LoginActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val mPagerAdapter = SlidePagerAdapter(supportFragmentManager)
        viewPager.adapter = mPagerAdapter
        indicator.setViewPager(viewPager)

        skip_button.setOnClickListener { moveToNextScreen() }
        next_button.setOnClickListener { moveForward() }
    }

    private fun moveForward() {
        if (viewPager.currentItem >= 4) {
            moveToNextScreen()
        } else {
            viewPager.currentItem = viewPager.currentItem + 1
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
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
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
