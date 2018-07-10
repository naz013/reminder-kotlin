package com.elementary.tasks.core.views

import android.animation.ObjectAnimator
import android.view.View

import com.elementary.tasks.core.utils.MeasureUtils

import java.util.ArrayList

import androidx.recyclerview.widget.RecyclerView

class ReturnScrollListener private constructor(builder: Builder) : RecyclerView.OnScrollListener() {

    private val mQuickReturnViewType: QuickReturnViewType
    private val mHeader: View?
    private val mMinHeaderTranslation: Int
    private val mFooter: View?
    private val mMinFooterTranslation: Int
    private val mIsSnappable: Boolean
    private val mIsGrid: Boolean

    private var mPrevScrollY = 0
    private var mHeaderDiffTotal = 0
    private var mFooterDiffTotal = 0
    private val mColumnCount: Int
    private val mExtraOnScrollListenerList = ArrayList<RecyclerView.OnScrollListener>()

    enum class QuickReturnViewType {
        HEADER,
        FOOTER,
        BOTH,
        GOOGLE_PLUS,
        TWITTER
    }

    init {
        mQuickReturnViewType = builder.mQuickReturnViewType
        mHeader = builder.mHeader
        mMinHeaderTranslation = builder.mMinHeaderTranslation
        mFooter = builder.mFooter
        mColumnCount = builder.mColumnCount
        mMinFooterTranslation = builder.mMinFooterTranslation
        mIsSnappable = builder.mIsSnappable
        mIsGrid = builder.isGrid
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        for (listener in mExtraOnScrollListenerList) {
            listener.onScrollStateChanged(recyclerView, newState)
        }
        if (newState == RecyclerView.SCROLL_STATE_IDLE && mIsSnappable) {

            val midHeader = -mMinHeaderTranslation / 2
            val midFooter = mMinFooterTranslation / 2

            when (mQuickReturnViewType) {
                ReturnScrollListener.QuickReturnViewType.HEADER -> if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                    val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                    anim.setDuration(100)
                    anim.start()
                    mHeaderDiffTotal = 0
                } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                    val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                    anim.setDuration(100)
                    anim.start()
                    mHeaderDiffTotal = mMinHeaderTranslation
                }
                ReturnScrollListener.QuickReturnViewType.FOOTER -> if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                    val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                    anim.setDuration(100)
                    anim.start()
                    mFooterDiffTotal = 0
                } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                    val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                    anim.setDuration(100)
                    anim.start()
                    mFooterDiffTotal = -mMinFooterTranslation
                }
                ReturnScrollListener.QuickReturnViewType.BOTH -> {
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = 0
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = mMinHeaderTranslation
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = 0
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = -mMinFooterTranslation
                    }
                }
                ReturnScrollListener.QuickReturnViewType.TWITTER -> {
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = 0
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = mMinHeaderTranslation
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = 0
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = -mMinFooterTranslation
                    }
                }
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        for (listener in mExtraOnScrollListenerList) {
            listener.onScrolled(recyclerView, dx, dy)
        }
        val scrollY = MeasureUtils.getScrollY(recyclerView, mColumnCount, mIsGrid)
        val diff = mPrevScrollY - scrollY

        if (diff != 0) {
            when (mQuickReturnViewType) {
                ReturnScrollListener.QuickReturnViewType.HEADER -> {
                    if (diff < 0) { // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                    }
                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                }
                ReturnScrollListener.QuickReturnViewType.FOOTER -> {
                    if (diff < 0) { // scrolling down
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                    } else { // scrolling up
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }
                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                ReturnScrollListener.QuickReturnViewType.BOTH -> {
                    if (diff < 0) { // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }
                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                ReturnScrollListener.QuickReturnViewType.TWITTER -> {
                    if (diff < 0) { // scrolling down
                        if (scrollY > -mMinHeaderTranslation) {
                            mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)
                        }
                        if (scrollY > mMinFooterTranslation) {
                            mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                        }
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }
                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                else -> {
                }
            }
        }
        mPrevScrollY = scrollY
    }

    // region Helper Methods
    fun registerExtraOnScrollListener(listener: RecyclerView.OnScrollListener) {
        mExtraOnScrollListenerList.add(listener)
    }
    // endregion

    // region Inner Classes

    class Builder(// Required parameters
            private val mQuickReturnViewType: QuickReturnViewType) {

        // Optional parameters - initialized to default values
        private var mHeader: View? = null
        private var mMinHeaderTranslation = 0
        private var mFooter: View? = null
        private var mMinFooterTranslation = 0
        private var mIsSnappable = false
        private var mColumnCount = 1
        private var isGrid = false

        fun header(header: View): Builder {
            mHeader = header
            return this
        }

        fun minHeaderTranslation(minHeaderTranslation: Int): Builder {
            mMinHeaderTranslation = minHeaderTranslation
            return this
        }

        fun footer(footer: View): Builder {
            mFooter = footer
            return this
        }

        fun minFooterTranslation(minFooterTranslation: Int): Builder {
            mMinFooterTranslation = minFooterTranslation
            return this
        }

        fun columnCount(columnCount: Int): Builder {
            mColumnCount = columnCount
            return this
        }

        fun isSnappable(isSnappable: Boolean): Builder {
            mIsSnappable = isSnappable
            return this
        }

        fun isGrid(isGrid: Boolean): Builder {
            this.isGrid = isGrid
            return this
        }

        fun build(): ReturnScrollListener {
            return ReturnScrollListener(this)
        }
    }
}
