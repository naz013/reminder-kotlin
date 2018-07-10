package com.elementary.tasks.core.contacts

import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.MenuItem

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.databinding.ActivityContactsListBinding

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

    private var binding: ActivityContactsListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts_list)
        initActionBar()
        initTabNavigation()
    }

    private fun initTabNavigation() {
        val mSectionsPagerAdapter = ViewPagerAdapter(this, supportFragmentManager)
        binding!!.viewPager.adapter = mSectionsPagerAdapter
        binding!!.tabLayout.setupWithViewPager(binding!!.viewPager)
    }

    private fun initActionBar() {
        setSupportActionBar(binding!!.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        }
        binding!!.toolbar.title = ""
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        return true
    }

    override fun onContactSelected(number: String?, name: String) {
        val intent = Intent()
        if (number != null) {
            intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number)
        }
        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
