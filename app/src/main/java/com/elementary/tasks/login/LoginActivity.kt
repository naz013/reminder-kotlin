package com.elementary.tasks.login

import android.app.AlertDialog
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.CheckBirthdaysAsync
import com.elementary.tasks.core.cloud.DropboxLogin
import com.elementary.tasks.core.cloud.GoogleLogin
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.RealmDb
import com.elementary.tasks.databinding.ActivityLoginBinding
import com.elementary.tasks.google_tasks.GetTaskListAsync
import com.elementary.tasks.google_tasks.TasksCallback
import com.elementary.tasks.navigation.MainActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        const val PERM: Int = 103
        const val PERM_DROPBOX: Int = 104
        const val PERM_LOCAL: Int = 105
        const val PERM_BIRTH: Int = 106
        private const val TAG: String = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleLogin: GoogleLogin
    private lateinit var dropboxLogin: DropboxLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        googleLogin = GoogleLogin(this, object : GoogleLogin.LoginCallback {

            override fun onSuccess() {
                loadDataFromGoogle()
            }

            override fun onFail() {
                showLoginError()
            }
        })
        dropboxLogin = DropboxLogin(this, object : DropboxLogin.LoginCallback {
            override fun onSuccess(logged: Boolean) {
                if (logged) loadDataFromDropbox()
            }
        })
        initButtons()
        loadPhotoView()
    }

    private fun loadPhotoView() {
        Glide.with(this)
                .load("https://unsplash.it/1080/1920?image=596&blur")
                .override(1080, 1920)
                .centerCrop()
                .crossFade()
                .into(object : SimpleTarget<GlideDrawable>() {
                    override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                        binding.imageView2.setImageDrawable(resource?.current)
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        binding.imageView2.setImageResource(R.drawable.photo)
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        dropboxLogin.checkDropboxStatus()
    }

    private fun loadDataFromDropbox() {
        RestoreDropboxTask(this, RestoreDropboxTask.SyncListener { openApplication() }).execute()
    }

    private fun showLoginError() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.failed_to_login))
        builder.setPositiveButton(R.string.ok, { dialogInterface, i -> dialogInterface.dismiss() })
        builder.create().show()
    }

    private fun initButtons() {
        binding.googleButton.setOnClickListener({ googleLoginClick() })
        binding.localButton.setOnClickListener({ restoreLocalData() })
        binding.dropboxButton.setOnClickListener({ loginToDropbox() })
        binding.skipButton.setOnClickListener({ askForBirthdays() })
    }

    private fun askForBirthdays() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.import_birthdays))
        builder.setMessage(getString(R.string.would_you_like_to_import_birthdays))
        builder.setPositiveButton(getString(R.string.import_string), { dialogInterface, i ->
            dialogInterface.dismiss()
            importBirthdays()
        })
        builder.setNegativeButton(getString(R.string.open_app), { dialog, i ->
            dialog.dismiss()
            openApplication()
        })
        builder.create().show()
    }

    private fun importBirthdays() {
        if (!Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(this, PERM_BIRTH, Permissions.READ_CONTACTS)
            return
        }
        Prefs.getInstance(this).isContactBirthdaysEnabled = true
        Prefs.getInstance(this).isBirthdayReminderEnabled = true
        CheckBirthdaysAsync(this, true, CheckBirthdaysAsync.TaskCallback { openApplication() }).execute()
    }

    private fun initGroups() {
        if (RealmDb.getInstance().allGroups.size == 0) {
            RealmDb.getInstance().setDefaultGroups(this)
        }
    }

    private fun loginToDropbox() {
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            dropboxLogin.login()
        } else {
            Permissions.requestPermission(this, PERM_DROPBOX, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    private fun restoreLocalData() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, PERM_LOCAL, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            return
        }
        RestoreLocalTask(this, object : RestoreLocalTask.SyncListener {
            override fun onFinish() {
                openApplication()
            }
        }).execute()
    }

    private fun loadDataFromGoogle() {
        RestoreGoogleTask(this, RestoreGoogleTask.SyncListener { loadGoogleTasks() }).execute()
    }

    private fun loadGoogleTasks() {
        GetTaskListAsync(this, object : TasksCallback {
            override fun onComplete() {
                openApplication()
            }

            override fun onFailed() {
                openApplication()
            }
        }).execute()
    }

    private fun openApplication() {
        initGroups()
        Prefs.getInstance(this).isUserLogged = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun googleLoginClick() {
        if (Permissions.checkPermission(this, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                Permissions.WRITE_EXTERNAL)) {
            googleLogin.login()
        } else {
            Permissions.requestPermission(this, PERM, Permissions.GET_ACCOUNTS,
                    Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLogin.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERM -> googleLoginClick()
            PERM_DROPBOX -> loginToDropbox()
            PERM_LOCAL -> restoreLocalData()
            PERM_BIRTH -> importBirthdays()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
