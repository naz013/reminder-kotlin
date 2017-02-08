package com.elementary.tasks.core.cloud;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
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

public class GoogleLogin {

    private static final String TAG = "GoogleLogin";
    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;

    private GoogleDrive mGoogleDrive;
    private Activity activity;
    private String mAccountName;
    private LoginCallback mCallback;

    public GoogleLogin(Activity activity, LoginCallback mCallback) {
        this.activity = activity;
        this.mCallback = mCallback;
        mGoogleDrive = new GoogleDrive(activity);
    }

    public void logOut() {
        mGoogleDrive.unlink();
    }

    public boolean isLogged() {
        return mGoogleDrive.isLinked();
    }

    public void login() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        activity.startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void getAndUseAuthTokenInAsyncTask(Account account) {
        AsyncTask<Account, String, String> task = new AsyncTask<Account, String, String>() {
            ProgressDialog progressDlg;
            AsyncTask<Account, String, String> me = this;

            @Override
            protected void onPreExecute() {
                progressDlg = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
                progressDlg.setMax(100);
                progressDlg.setMessage(activity.getString(R.string.trying_to_log_in));
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
            return GoogleAuthUtil.getToken(activity, account.name, "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS);
        } catch (UserRecoverableAuthException e) {
            activity.startActivityForResult(e.getIntent(), REQUEST_ACCOUNT_PICKER);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(activity);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName));
            finishLogin();
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            finishLogin();
        } else {
            if (mCallback != null) mCallback.onFail();
        }
    }

    private void finishLogin() {
        Prefs.getInstance(activity).setDriveUser(mAccountName);
        if (mCallback != null) mCallback.onSuccess();
    }

    public interface LoginCallback {
        void onSuccess();

        void onFail();
    }
}
