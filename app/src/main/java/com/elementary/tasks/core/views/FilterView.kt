package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.MeasureUtils
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.view_chip.view.*
import java.util.*

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
class FilterView : LinearLayout {
    private var mContext: Context? = null
    private var numOfFilters: Int = 0
    private val mFilters = ArrayList<Filter>()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    private fun init(context: Context) {
        this.mContext = context
        orientation = LinearLayout.VERTICAL
    }

    fun clear() {
        this.removeAllViewsInLayout()
        this.numOfFilters = 0
    }

    fun addFilter(filter: Filter?) {
        if (filter == null) return
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, MeasureUtils.dp2px(mContext!!, 8), 0, MeasureUtils.dp2px(mContext!!, 8))
        if (numOfFilters > 0) {
            this.addDivider()
        }
        this.addView(createFilter(filter), layoutParams)
        this.numOfFilters++
        this.mFilters.add(filter)
        this.requestLayout()
    }

    private fun addDivider() {
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, MeasureUtils.dp2px(mContext!!, 1))
        layoutParams.setMargins(MeasureUtils.dp2px(mContext!!, 16), 0, MeasureUtils.dp2px(mContext!!, 16), 0)
        val view = View(mContext)
        view.setBackgroundColor(resources.getColor(R.color.whitePrimary))
        this.addView(view, layoutParams)
    }

    private fun createFilter(filter: Filter): View {
        val scrollView = HorizontalScrollView(mContext)
        scrollView.overScrollMode = View.OVER_SCROLL_NEVER
        scrollView.isHorizontalScrollBarEnabled = false
        val layout = ChipGroup(mContext)
        layout.setChipSpacing(MeasureUtils.dp2px(context, 8))
        layout.isSingleSelection = true
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(MeasureUtils.dp2px(mContext!!, 8), 0, 0, 0)
        for (element in filter) {
            val view = createChip(element)
            layout.addView(view, layoutParams)
        }
        layout.setPadding(0, 0, MeasureUtils.dp2px(mContext!!, 8), 0)
        layout.setOnCheckedChangeListener { _, p1 ->
            for (f in filter) {
                if (f.id == p1 - 1000) {
                    filter.elementClick.onClick(f.binding, p1 - 1000)
                }
            }
        }
        scrollView.addView(layout)
        return scrollView
    }

    private fun createChip(element: FilterElement): View {
        val binding = LayoutInflater.from(mContext).inflate(R.layout.view_chip, null, false)
        binding.chipView.text = element.title
        if (element.iconId == 0) {
            binding.chipView.isChipIconVisible = false
        } else {
            binding.chipView.setChipIconResource(element.iconId)
            binding.chipView.isChipIconVisible = true
        }
        binding.chipView.isChecked = element.isChecked
        binding.chipView.id = element.id + 1000
        element.binding = binding
        return binding
    }

    interface FilterElementClick {
        fun onClick(view: View?, id: Int)
    }

    class Filter(val elementClick: FilterElementClick) : AbstractList<FilterElement>() {

        private val elements = ArrayList<FilterElement>()
        internal val uuId = UUID.randomUUID().toString()

        override fun get(index: Int): FilterElement {
            return elements[index]
        }

        override fun clear() {
            elements.clear()
        }

        override fun addAll(elements: Collection<FilterElement>): Boolean {
            return this.elements.addAll(elements)
        }

        override val size: Int
            get() = elements.size

        override fun add(element: FilterElement): Boolean {
            return elements.add(element)
        }

        override fun iterator(): MutableIterator<FilterElement> {
            return elements.iterator()
        }
    }

    data class FilterElement(@DrawableRes var iconId: Int, var title: String? = null, var id: Int = 0,
                             var isChecked: Boolean = false, var binding: View? = null)
}
