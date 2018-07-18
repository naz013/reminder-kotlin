package com.elementary.tasks.intro

import android.content.Intent
import android.os.Bundle
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.login.LoginActivity
import com.mcxiaoke.koi.ext.onClick
import kotlinx.android.synthetic.main.activity_google_fix.*

class GoogleFixActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_fix)
        install_button.onClick { installServices() }
    }

    private fun installServices() {
        SuperUtil.checkGooglePlayServicesAvailability(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            openLoginScreen()
        }
    }

    private fun openLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
