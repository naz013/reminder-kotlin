package com.elementary.tasks.core.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import kotlinx.android.synthetic.main.activity_application_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

/**
 * Copyright 2016 Nazar Suhovich
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
class ApplicationActivity : ThemedActivity(), FilterCallback<ApplicationItem> {

    private var adapter: AppsRecyclerAdapter = AppsRecyclerAdapter()
    private val filterController = AppFilterController(this)
    private var mLoader: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_list)
        loaderView.visibility = View.GONE
        initActionBar()
        initSearchView()
        initRecyclerView()
        loadApps()
    }

    private fun hideProgress() {
        loaderView.visibility = View.GONE
    }

    private fun showProgress() {
        loaderView.visibility = View.VISIBLE
    }

    private fun loadApps() {
        showProgress()
        mLoader?.cancel()
        mLoader = launch(CommonPool) {
            val mList: MutableList<ApplicationItem> = mutableListOf()
            val pm = packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (packageInfo in packages) {
                val name = packageInfo.loadLabel(pm).toString()
                val packageName = packageInfo.packageName
                val drawable = packageInfo.loadIcon(pm)
                val data = ApplicationItem(name, packageName, drawable)
                val pos = getPosition(name, mList)
                if (pos == -1) {
                    mList.add(data)
                } else {
                    mList.add(getPosition(name, mList), data)
                }
            }
            withUIContext {
                hideProgress()
                filterController.original = mList
                refreshView()
            }
        }
    }

    private fun getPosition(name: String, mList: MutableList<ApplicationItem>): Int {
        if (mList.size == 0) {
            return 0
        }
        var position = -1
        for (data in mList) {
            val comp = name.compareTo(data.name!!)
            if (comp <= 0) {
                position = mList.indexOf(data)
                break
            }
        }
        return position
    }

    private fun initRecyclerView() {
        adapter.actionsListener = object : ActionsListener<ApplicationItem> {
            override fun onAction(view: View, position: Int, t: ApplicationItem?, actions: ListActions) {
                if (t != null) {
                    val intent = Intent()
                    intent.putExtra(Constants.SELECTED_APPLICATION, t.packageName)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
        contactsList.layoutManager = LinearLayoutManager(this)
        contactsList.adapter = adapter
        contactsList.isNestedScrollingEnabled = false
        ViewUtils.listenScrollableView(scroller) {
            toolbarView.isSelected = it > 0
        }
    }

    private fun initSearchView() {
        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterController.setSearchValue(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun initActionBar() {
        backButton.setOnClickListener { onBackPressed() }
    }

    override fun onPause() {
        super.onPause()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(searchField.windowToken, 0)
    }

    private fun refreshView() {
        if (adapter.itemCount > 0) {
            emptyItem.visibility = View.GONE
            scroller.visibility = View.VISIBLE
        } else {
            scroller.visibility = View.GONE
            emptyItem.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLoader?.cancel()
    }

    override fun onChanged(result: List<ApplicationItem>) {
        adapter.data = result.toMutableList()
        contactsList.smoothScrollToPosition(0)
        refreshView()
    }
}
