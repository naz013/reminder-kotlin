package com.elementary.tasks.monthView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.fragment_date_grid.*
import timber.log.Timber


/**
 * Copyright 2018 Nazar Suhovich
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
class MonthFragment : Fragment() {

    private var callback: MonthCallback? = null
    private var mItem: MonthPagerItem? = null

    fun getModel(): MonthPagerItem? = mItem

    fun setModel(monthPagerItem: MonthPagerItem) {
        this.mItem = monthPagerItem
        Timber.d("setModel: $monthPagerItem")
        monthView?.setDate(monthPagerItem.year, monthPagerItem.month + 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = parentFragment
        if (fragment != null) {
            callback = fragment as MonthCallback?
        }
        if (arguments != null) {
            mItem = arguments?.getSerializable(ARGUMENT_PAGE_NUMBER) as MonthPagerItem?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_date_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = mItem
        if (item != null) {
            monthView?.setDate(item.year, item.month + 1)
        }
    }

    fun requestData() {
        val item = mItem
        if (item != null) {
            callback?.find(item) { eventsPagerItem, list ->
                Timber.d("setModel: $eventsPagerItem, ${list.size}")

            }
        }
    }

    companion object {
        private const val ARGUMENT_PAGE_NUMBER = "arg_page"
        fun newInstance(item: MonthPagerItem): MonthFragment {
            val pageFragment = MonthFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARGUMENT_PAGE_NUMBER, item)
            pageFragment.arguments = bundle
            return pageFragment
        }
    }
}
