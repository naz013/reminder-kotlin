package com.elementary.tasks.login;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.CheckBirthdaysAsync;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.DropboxLogin;
import com.elementary.tasks.core.cloud.GoogleLogin;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityLoginBinding;
import com.elementary.tasks.google_tasks.GetTaskListAsync;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.navigation.MainActivity;
import com.elementary.tasks.notes.create.CreateNoteActivity;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

/**
 * Copyright 2018 Nazar Suhovich
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
public class LoginActivity extends ThemedActivity {

    private static final int PERM = 103;
    private static final int PERM_DROPBOX = 104;
    private static final int PERM_LOCAL = 105;
    private static final int PERM_BIRTH = 106;
    private static final String TERMS_URL = "termsopen.com";

    private ActivityLoginBinding binding;

    private GoogleLogin googleLogin;
    private DropboxLogin dropboxLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        googleLogin = new GoogleLogin(this, new GoogleLogin.LoginCallback() {

            @Override
            public void onSuccess() {
                loadDataFromGoogle();
            }

            @Override
            public void onFail() {
                showLoginError();
            }
        });
        dropboxLogin = new DropboxLogin(this, logged -> {
            if (logged) loadDataFromDropbox();
        });
        initButtons();
        loadPhotoView();
        initCheckbox();
    }

    private void loadPhotoView() {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .override(768, 1280);

        Glide.with(this)
                .load("https://unsplash.it/1080/1920?image=596&blur")
                .apply(myOptions)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        binding.imageView2.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        loadDefaultImage();
                    }
                });
    }

    private void loadDefaultImage() {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .override(768, 1280);

        Glide.with(this)
                .load(R.drawable.photo)
                .apply(myOptions)
                .into(binding.imageView2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dropboxLogin.checkDropboxStatus();
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
        builder.setNegativeButton(getString(R.string.open_app), (dialogInterface, i) -> {
            dialogInterface.dismiss();
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
        if (AppDb.getAppDatabase(this).groupDao().getAll().size() == 0) {
            RealmDb.getInstance().setDefaultGroups(this);
        }
    }

    private void loginToDropbox() {
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            dropboxLogin.login();
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
            public void onComplete() {
                openApplication();
            }

            @Override
            public void onFailed() {
                openApplication();
            }
        }).execute();
    }

    private void openApplication() {
        enableShortcuts();
        initGroups();
        Prefs.getInstance(this).setUserLogged(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void enableShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "id.reminder")
                    .setShortLabel(getString(R.string.add_reminder_menu))
                    .setLongLabel(getString(R.string.add_reminder_menu))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_reminder_shortcut))
                    .setIntents(new Intent[]{new Intent(Intent.ACTION_MAIN).setClass(this, MainActivity.class),
                            new Intent(Intent.ACTION_VIEW).setClass(this, CreateReminderActivity.class)})
                    .build();

            ShortcutInfo shortcut2 = new ShortcutInfo.Builder(this, "id.note")
                    .setShortLabel(getString(R.string.add_note))
                    .setLongLabel(getString(R.string.add_note))
                    .setIcon(Icon.createWithResource(this, R.drawable.add_note_shortcut))
                    .setIntents(new Intent[]{new Intent(Intent.ACTION_MAIN).setClass(this, MainActivity.class),
            new Intent(Intent.ACTION_VIEW).setClass(this, CreateNoteActivity.class)})
                    .build();
            if (shortcutManager != null) {
                shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut, shortcut2));
            }
        }
    }

    private void googleLoginClick() {
        if (Permissions.checkPermission(this, Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                Permissions.WRITE_EXTERNAL)) {
            googleLogin.login();
        } else {
            Permissions.requestPermission(this, PERM, Permissions.GET_ACCOUNTS,
                    Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
        }
    }

    private void initCheckbox() {
        setViewHTML(binding.termsCheckBox, getString(R.string.i_accept));
        binding.termsCheckBox.setOnCheckedChangeListener((compoundButton, b) -> setEnabling(b));
        binding.termsCheckBox.setChecked(true);
    }

    private void setEnabling(boolean b) {
        binding.dropboxButton.setEnabled(b);
        binding.googleButton.setEnabled(b);
        binding.localButton.setEnabled(b);
        binding.skipButton.setEnabled(b);
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        strBuilder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                if (span.getURL().contains(TERMS_URL)) {
                    openTermsScreen();
                }
            }
        }, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void openTermsScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.privacy_policy));
        WebView webView = new WebView(this);
        webView.loadUrl("https://craysoftware.wordpress.com/privacy-policy/");
        builder.setView(webView);
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void setViewHTML(CheckBox text, String html) {
        Spanned sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERM:
                googleLoginClick();
                break;
            case PERM_DROPBOX:
                loginToDropbox();
                break;
            case PERM_LOCAL:
                restoreLocalData();
                break;
            case PERM_BIRTH:
                importBirthdays();
                break;
        }
    }
}
