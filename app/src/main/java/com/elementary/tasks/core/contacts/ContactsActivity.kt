package com.elementary.tasks.core.contacts

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import kotlinx.android.synthetic.main.activity_contacts_list.*

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

class ContactsActivity : ThemedActivity(), NumberCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_list)
        initActionBar()
        initTabNavigation()
    }

    private fun initTabNavigation() {
        val mSectionsPagerAdapter = ViewPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = mSectionsPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.title = ""
        if (isDark) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
        }
        return true
    }

    override fun onContactSelected(number: String, name: String) {
        val intent = Intent()
        if (number != "") {
            intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
        }
        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name)
        setResult(RESULT_OK, intent)
        finish()
    }
}
