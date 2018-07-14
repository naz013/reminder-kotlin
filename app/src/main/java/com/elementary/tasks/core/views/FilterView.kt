package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout

import com.elementary.tasks.R
import com.elementary.tasks.core.utils.MeasureUtils

import java.util.AbstractList
import java.util.ArrayList
import java.util.UUID

import androidx.annotation.DrawableRes
import kotlinx.android.synthetic.main.view_chip.view.*

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
        val layout = LinearLayout(mContext)
        layout.orientation = LinearLayout.HORIZONTAL
        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(MeasureUtils.dp2px(mContext!!, 8), 0, 0, 0)
        for (element in filter) {
            val view = createChip(element, filter)
            layout.addView(view, layoutParams)
        }
        layout.setPadding(0, 0, MeasureUtils.dp2px(mContext!!, 8), 0)
        scrollView.addView(layout)
        return scrollView
    }

    private fun createChip(element: FilterElement, filter: Filter): View {
        val binding = LayoutInflater.from(mContext).inflate(R.layout.view_chip, null, false)
        binding.chipTitle.text = element.title
        if (element.iconId == 0) {
            binding.iconView.visibility = View.GONE
        } else {
            binding.iconView.setImageResource(element.iconId)
            binding.iconView.visibility = View.VISIBLE
        }
        setStatus(binding, element.isChecked)
        element.binding = binding
        binding.setOnClickListener { updateFilter(binding, element.id, filter.uuId) }
        return binding
    }

    private fun setStatus(binding: View?, checked: Boolean) {
        if (checked)
            binding!!.setBackgroundResource(R.drawable.chip_selected_bg)
        else
            binding!!.setBackgroundResource(R.drawable.chip_bg)
    }

    private fun getCurrent(id: String): Filter? {
        if (mFilters.isEmpty()) return null
        for (filter in mFilters) {
            if (filter.uuId == id) return filter
        }
        return null
    }

    private fun updateFilter(v: View, id: Int, filterId: String) {
        val filter = getCurrent(filterId)
        if (filter != null) {
            if (filter.choiceMode == ChoiceMode.SINGLE) {
                for (element in filter) {
                    setStatus(element.binding, false)
                    element.isChecked = false
                }
                for (element in filter) {
                    if (element.id == id) {
                        element.isChecked = true
                        setStatus(element.binding, element.isChecked)
                        break
                    }
                }
                filter.elementClick.onClick(v, id)
            } else {
                for (element in filter) {
                    if (id == 0) {
                        setStatus(element.binding, false)
                        element.isChecked = false
                    } else {
                        if (element.id == 0) {
                            setStatus(element.binding, false)
                            element.isChecked = false
                        }
                    }
                }
                for (element in filter) {
                    if (element.id == id) {
                        element.isChecked = !element.isChecked
                        setStatus(element.binding, element.isChecked)
                        break
                    }
                }
                filter.elementClick.onMultipleSelected(v, getSelected(filter))
            }
        }
    }

    private fun getSelected(filter: Filter): List<Int> {
        val list = ArrayList<Int>()
        for (element in filter) if (element.isChecked) list.add(element.id)
        return list
    }

    interface FilterElementClick {
        fun onClick(view: View, id: Int)

        fun onMultipleSelected(view: View, ids: List<Int>)
    }

    class Filter(val elementClick: FilterElementClick) : AbstractList<FilterElement>() {
        private val elements = ArrayList<FilterElement>()
        internal var choiceMode = ChoiceMode.SINGLE
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

    class FilterElement {
        @DrawableRes
        @get:DrawableRes
        internal val iconId: Int
        var title: String? = null
        var id: Int = 0
        var isChecked: Boolean = false
        var binding: View? = null

        constructor(@DrawableRes iconId: Int, title: String, id: Int) {
            this.iconId = iconId
            this.title = title
            this.id = id
        }

        constructor(@DrawableRes iconId: Int, title: String, id: Int, isChecked: Boolean) {
            this.iconId = iconId
            this.title = title
            this.id = id
            this.isChecked = isChecked
        }

        override fun toString(): String {
            return "FilterElement{" +
                    "title='" + title + '\''.toString() +
                    ", id=" + id +
                    ", isChecked=" + isChecked +
                    '}'.toString()
        }
    }

    enum class ChoiceMode {
        SINGLE,
        MULTI
    }
}
