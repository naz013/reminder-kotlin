package com.elementary.tasks.experimental

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.databinding.ActivityBottomNavBinding
import com.elementary.tasks.navigation.FragmentCallback
import com.elementary.tasks.navigation.fragments.BaseFragment

class BottomNavActivity : ThemedActivity<ActivityBottomNavBinding>(), FragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.bottomNav.setupWithNavController(findNavController(R.id.mainNavigationFragment))
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.mainNavigationFragment).navigateUp()
    }

    override fun layoutRes(): Int {
        return R.layout.activity_bottom_nav
    }

    override fun onFragmentSelect(fragment: BaseFragment<*>) {

    }

    override fun onMenuSelect(menu: Int) {

    }

    override fun onScrollUpdate(y: Int) {

    }

    override fun onTitleChange(title: String) {

    }

    override fun openFragment(fragment: BaseFragment<*>, tag: String) {

    }

    override fun openFragment(fragment: BaseFragment<*>, tag: String, replace: Boolean) {

    }

    override fun refreshMenu() {

    }

    override fun hideKeyboard() {

    }
}
