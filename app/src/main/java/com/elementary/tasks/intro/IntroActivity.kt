package com.elementary.tasks.intro

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.databinding.ActivityIntroBinding
import com.elementary.tasks.login.LoginActivity

class IntroActivity : ThemedActivity<ActivityIntroBinding>() {

    override fun layoutRes(): Int = R.layout.activity_intro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mPagerAdapter = SlidePagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = mPagerAdapter
        binding.indicator.setViewPager(binding.viewPager)

        binding.skipButton.setOnClickListener { moveToNextScreen() }
        binding.nextButton.setOnClickListener { moveForward() }

        initCheckbox()
    }

    private fun initCheckbox() {
        setViewHTML(binding.termsCheckBox, getString(R.string.i_accept))
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        strBuilder.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                if (span.url.contains(TERMS_URL)) {
                    openTermsScreen()
                }
            }
        }, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun openTermsScreen() {
        startActivity(Intent(this, PrivacyPolicyActivity::class.java))
    }

    private fun setViewHTML(text: CheckBox, html: String) {
        val sequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder, span)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun moveForward() {
        if (binding.viewPager.currentItem >= 4) {
            moveToNextScreen()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem + 1
        }
    }

    private fun moveToNextScreen() {
        if (!binding.termsCheckBox.isChecked) {
            Toast.makeText(this, getString(R.string.privacy_warming), Toast.LENGTH_SHORT).show()
            return
        }
        Module.checkComponents(this)
        openLoginScreen()
    }

    private fun openLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
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

    companion object {
        private const val TERMS_URL = "termsopen.com"
    }
}
