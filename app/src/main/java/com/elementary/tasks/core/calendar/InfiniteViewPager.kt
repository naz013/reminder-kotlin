package com.elementary.tasks.core.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class InfiniteViewPager : ViewPager {
    private var enabled = true

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun setAdapter(adapter: PagerAdapter?) {
        super.setAdapter(adapter)
        currentItem = OFFSET
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return enabled && super.onTouchEvent(event)
    }

    companion object {

        const val OFFSET = 1000
    }
}
