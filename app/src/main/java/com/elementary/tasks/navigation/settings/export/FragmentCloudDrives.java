package com.elementary.tasks.navigation.settings.export;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.GoogleDrive;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.FragmentCloudDrivesBinding;
import com.elementary.tasks.google_tasks.GetTaskListAsync;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Copyright 2016 Nazar Suhovich
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

public class FragmentCloudDrives extends BaseSettingsFragment {

    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;
    private static final String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    private static final String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    private Dropbox mDropbox;
    private GoogleDrive mGoogleDrive;

    private FragmentCloudDrivesBinding binding;
    private RoboButton mDropboxButton, mGoogleDriveButton;
    private RoboTextView mGoogleDriveTitle, mDropboxTitle;

    private String mAccountName;
    private ProgressDialog mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCloudDrivesBinding.inflate(inflater, container, false);
        mDropbox = new Dropbox(mContext);
        mDropbox.startSession();
        mGoogleDrive = new GoogleDrive(mContext);
        initDropboxButton();
        initGoogleDriveButton();
        initTitles();
        checkGoogleStatus();
        setImage();
        return binding.getRoot();
    }

    private void initTitles() {
        mDropboxTitle = binding.dropboxTitle;
        mGoogleDriveTitle = binding.gDriveTitle;
    }

    private void initGoogleDriveButton() {
        mGoogleDriveButton = binding.linkGDrive;
        mGoogleDriveButton.setOnClickListener(v -> {
            googleDriveButtonClick();
        });
    }

    private void googleDriveButtonClick() {
        if (Permissions.checkPermission(getActivity(),
                Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                Permissions.WRITE_EXTERNAL)) {
            switchGoogleStatus();
        } else {
            Permissions.requestPermission(getActivity(), 103,
                    Permissions.GET_ACCOUNTS, Permissions.READ_EXTERNAL,
                    Permissions.WRITE_EXTERNAL);
        }
    }

    private void initDropboxButton() {
        mDropboxButton = binding.linkDropbox;
        mDropboxButton.setOnClickListener(v -> dropboxClick());
    }

    private void dropboxClick() {
        boolean isIn = isAppInstalled(MARKET_APP_JUSTREMINDER_PRO);
        if (Module.isPro()) isIn = isAppInstalled(MARKET_APP_JUSTREMINDER);
        if (isIn) {
            checkDialog().show();
        } else {
            performDropboxLinking();
        }
    }

    private void performDropboxLinking() {
        if (mDropbox.isLinked()) {
            if (mDropbox.unlink()) {
                mDropboxButton.setText(getString(R.string.connect));
            }
        } else {
            mDropbox.startLink();
        }
    }

    private void switchGoogleStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(getActivity())) {
            Toast.makeText(mContext, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mGoogleDrive.isLinked()){
            disconnectFromGoogleServices();
        } else {
            requestGoogleConnection();
        }
    }

    private void requestGoogleConnection() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void disconnectFromGoogleServices() {
        mGoogleDrive.unlink();
        checkGoogleStatus();
        RealmDb.getInstance().deleteTasks();
        RealmDb.getInstance().deleteTaskLists();
        if (mCallback != null) mCallback.refreshMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    switchGoogleStatus();
                }
                break;
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    protected Dialog checkDialog() {
        return new AlertDialog.Builder(mContext)
                .setMessage(getString(R.string.other_version_detected))
                .setPositiveButton(getString(R.string.open), (dialog, which) -> {
                    Intent i;
                    PackageManager manager = mContext.getPackageManager();
                    if (Module.isPro()) i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
                    else i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                })
                .setNegativeButton(getString(R.string.delete), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    if (Module.isPro()) intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
                    else intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
                    startActivity(intent);
                })
                .setNeutralButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .create();
    }

    private void checkGoogleStatus(){
        if (mGoogleDrive.isLinked()){
            mGoogleDriveButton.setText(R.string.disconnect);
        } else {
            mGoogleDriveButton.setText(getString(R.string.connect));
        }
    }

    private void setImage(){
        if (ThemeUtil.getInstance(mContext).isDark()){
            mDropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dropbox_white, 0, 0, 0);
            mGoogleDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_google_white, 0, 0, 0);
        } else {
            mDropboxTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dropbox, 0, 0, 0);
            mGoogleDriveTitle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_google, 0, 0, 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.cloud_services));
            mCallback.onFragmentSelect(this);
        }
        checkDropboxStatus();
        checkGoogleStatus();
    }

    private void checkDropboxStatus() {
        if (mDropbox.checkLink() && mDropbox.isLinked()) {
            mDropboxButton.setText(getString(R.string.disconnect));
        } else if (mDropbox.isLinked()) {
            mDropboxButton.setText(getString(R.string.disconnect));
        } else {
            mDropboxButton.setText(getString(R.string.connect));
        }
    }

    void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setMessage(getString(R.string.trying_to_log_in));
                progressDlg.setCancelable(false);
                progressDlg.setIndeterminate(false);
                progressDlg.setOnCancelListener(dialog -> {
                    progressDlg.dismiss();
                    me.cancel(true);
                });
                progressDlg.show();
            }

            @Override
            protected String doInBackground(Account... params) {
                return getAccessToken(params[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    mAccountName = s;
                }
                progressDlg.dismiss();
            }
        };
        task.execute(account);
    }

    private String getAccessToken(Account account) {
        try {
            return GoogleAuthUtil.getToken(mContext, account.name, "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS);
        } catch (UserRecoverableAuthException e) {
            startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
            e.printStackTrace();
            return null;
        } catch (GoogleAuthException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(mContext);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName));
            startSync(mAccountName);
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            startSync(mAccountName);
        }
    }

    private void startSync(String accountName) {
        Prefs.getInstance(mContext).setDriveUser(SuperUtil.encrypt(accountName));
        if (mCallback != null) mCallback.refreshMenu();
        mDialog = ProgressDialog.show(mContext, null, getString(R.string.retrieving_tasks), false, true);
        new GetTaskListAsync(mContext, new TasksCallback() {
            @Override
            public void onFailed() {
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        }).execute();
    }
}
