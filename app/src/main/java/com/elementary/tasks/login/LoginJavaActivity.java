package com.elementary.tasks.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.CheckBirthdaysAsync;
import com.elementary.tasks.core.cloud.DropboxLogin;
import com.elementary.tasks.core.cloud.GoogleLogin;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityLoginBinding;
import com.elementary.tasks.google_tasks.GetTaskListAsync;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.navigation.MainActivity;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LoginJavaActivity extends AppCompatActivity {

    private static final int PERM = 103;
    private static final int PERM_DROPBOX = 104;
    private static final int PERM_LOCAL = 105;
    private static final int PERM_BIRTH = 106;
    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private GoogleLogin mGoogleLogin;
    private DropboxLogin mLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mGoogleLogin = new GoogleLogin(this, new GoogleLogin.LoginCallback() {
            @Override
            public void onSuccess() {
                loadDataFromGoogle();
            }

            @Override
            public void onFail() {
                showLoginError();
            }
        });
        mLogin = new DropboxLogin(this, new DropboxLogin.LoginCallback() {
            @Override
            public void onSuccess(boolean logged) {
                if (logged) loadDataFromDropbox();
            }
        });
        initButtons();
        loadPhotoView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLogin.checkDropboxStatus();
    }

    private void loadPhotoView() {
        Glide.with(this)
                .load("https://unsplash.it/1080/1920?image=596&blur")
                .override(1080, 1920)
                .centerCrop()
                .crossFade()
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        binding.imageView2.setImageDrawable(resource.getCurrent());
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        binding.imageView2.setImageResource(R.drawable.photo);
                    }
                });
    }

    private void loadDataFromDropbox() {
        new RestoreDropboxTask(this, this::openApplication).execute();
    }

    private void showLoginError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.failed_to_login));
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void initButtons() {
        binding.googleButton.setOnClickListener(view -> googleLoginClick());
        binding.localButton.setOnClickListener(view -> restoreLocalData());
        binding.dropboxButton.setOnClickListener(view -> loginToDropbox());
        binding.skipButton.setOnClickListener(view -> askForBirthdays());
    }

    private void askForBirthdays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.import_birthdays));
        builder.setMessage(getString(R.string.would_you_like_to_import_birthdays));
        builder.setPositiveButton(getString(R.string.import_string), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            importBirthdays();
        });
        builder.setNegativeButton(getString(R.string.open_app), (dialog, i) -> {
            dialog.dismiss();
            openApplication();
        });
        builder.create().show();
    }

    private void importBirthdays() {
        if (!Permissions.checkPermission(this, Permissions.READ_CONTACTS)) {
            Permissions.requestPermission(this, PERM_BIRTH, Permissions.READ_CONTACTS);
            return;
        }
        Prefs.getInstance(this).setContactBirthdaysEnabled(true);
        Prefs.getInstance(this).setBirthdayReminderEnabled(true);
        new CheckBirthdaysAsync(this, true, this::openApplication).execute();
    }

    private void initGroups() {
        if (RealmDb.getInstance().getAllGroups().size() == 0) {
            RealmDb.getInstance().setDefaultGroups(this);
        }
    }

    private void loginToDropbox() {
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            mLogin.login();
        } else {
            Permissions.requestPermission(this, PERM_DROPBOX, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
        }
    }

    private void restoreLocalData() {
        if (!Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, PERM_LOCAL, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            return;
        }
        new RestoreLocalTask(this, this::openApplication).execute();
    }

    private void loadDataFromGoogle() {
        new RestoreGoogleTask(this, this::loadGoogleTasks).execute();
    }

    private void loadGoogleTasks() {
        new GetTaskListAsync(this, new TasksCallback() {
            @Override
            public void onFailed() {
                openApplication();
            }

            @Override
            public void onComplete() {
                openApplication();
            }
        }).execute();
    }

    private void openApplication() {
        initGroups();
        Prefs.getInstance(this).setUserLogged(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void googleLoginClick() {
        if (Permissions.checkPermission(this, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                Permissions.WRITE_EXTERNAL)) {
            mGoogleLogin.login();
        } else {
            Permissions.requestPermission(this, PERM, Permissions.GET_ACCOUNTS,
                    Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGoogleLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM:
                googleLoginClick();
                break;
            case PERM_BIRTH:
                importBirthdays();
                break;
            case PERM_DROPBOX:
                loginToDropbox();
                break;
            case PERM_LOCAL:
                restoreLocalData();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
