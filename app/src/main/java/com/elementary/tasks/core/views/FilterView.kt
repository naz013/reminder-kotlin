package com.elementary.tasks.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.ChipViewBinding
import com.elementary.tasks.core.utils.MeasureUtils
import com.google.android.material.chip.ChipGroup
import timber.log.Timber
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
    private var numOfFilters: Int = 0
    private val mFilters = ArrayList<Filter>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
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
        layoutParams.setMargins(0, MeasureUtils.dp2px(context, 8), 0, MeasureUtils.dp2px(context, 8))
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
                LinearLayout.LayoutParams.WRAP_CONTENT, MeasureUtils.dp2px(context, 1))
        layoutParams.setMargins(0, 0, 0, 0)
        val view = DividerView(context)
        this.addView(view, layoutParams)
    }

    private fun createFilter(filter: Filter): View {
        val chipGroup = ChipGroup(context)
        chipGroup.setChipSpacing(MeasureUtils.dp2px(context, 8))
        chipGroup.isSingleSelection = true
        for (element in filter) {
            val chip = createChip(element, chipGroup)
            chip.setOnClickListener {
                Timber.d("createFilter: ${it.id}")
                filter.elementClick.onClick(element.binding, element.id)
            }
            chipGroup.addView(chip)
        }
        chipGroup.setPadding(0, 0, MeasureUtils.dp2px(context, 8), 0)
        return chipGroup
    }

    private fun createChip(element: FilterElement, parent: ViewGroup): View {
        val chip = ChipViewBinding(LayoutInflater.from(context).inflate(R.layout.view_chip, parent, false))
        chip.chipView.text = element.title
        chip.chipView.isChipIconVisible = false
        chip.chipView.isChecked = element.isChecked
        chip.chipView.id = element.id + 1000
        element.binding = chip.view
        return chip.view
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

    data class FilterElement(var title: String? = null, var id: Int = 0,
                             var isChecked: Boolean = false, var binding: View? = null)
}
