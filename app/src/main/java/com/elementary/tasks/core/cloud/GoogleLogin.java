package com.elementary.tasks.core.cloud;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.tasks.TasksScopes;

import java.io.IOException;

import timber.log.Timber;

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

    private static final int REQUEST_AUTHORIZATION = 1;
    private static final int REQUEST_ACCOUNT_PICKER = 3;
    private static final String RT_CODE = "rt";

    private Google mGoogle;
    private Activity activity;
    private String mAccountName;
    @Nullable
    private LoginCallback mCallback;
    @NonNull
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private ProgressDialog mProgress;
    @Nullable
    private Intent rtIntent;

    public GoogleLogin(Activity activity, @Nullable LoginCallback mCallback) {
        this.activity = activity;
        this.mCallback = mCallback;
        this.mGoogle = Google.getInstance(activity);
    }

    public void logOut() {
        Prefs.getInstance(activity).setDriveUser(Prefs.DRIVE_USER_NONE);
        mGoogle.logOut();
        mGoogle = null;
    }

    public boolean isLogged() {
        return mGoogle != null;
    }

    public void login() {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, false, null, null, null, null);
        activity.startActivityForResult(intent, REQUEST_AUTHORIZATION);
    }

    private void getAndUseAuthTokenInAsyncTask(Account account) {
        mUiHandler.post(this::showProgress);
        new Thread(() -> {
            final String token = getAccessToken(account);
            mUiHandler.post(() -> {
                hideProgress();
                if (token != null) {
                    if (token.equals(RT_CODE)) {
                        if (rtIntent != null) {
                            activity.startActivityForResult(rtIntent, REQUEST_ACCOUNT_PICKER);
                        } else {
                            if (mCallback != null) mCallback.onFail();
                        }
                    } else {
                        finishLogin();
                        if (mCallback != null) mCallback.onSuccess();
                    }
                } else {
                    if (mCallback != null) mCallback.onFail();
                }
            });
        }).start();
    }

    private void hideProgress() {
        Timber.d("hideProgress: ");
        try {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
        } catch (IllegalArgumentException ignored) {
        }
        mProgress = null;
    }

    private void showProgress() {
        Timber.d("showProgress: ");
        if (mProgress != null && mProgress.isShowing()) return;
        mProgress = new ProgressDialog(activity, ProgressDialog.STYLE_SPINNER);
        mProgress.setMessage(activity.getString(R.string.trying_to_log_in));
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);
        mProgress.show();
    }

    @Nullable
    private String getAccessToken(@NonNull Account account) {
        Timber.d("getAccessToken: ");
        try {
            String scope = "oauth2:" + DriveScopes.DRIVE + " " + TasksScopes.TASKS;
            String token = GoogleAuthUtil.getToken(activity, account, scope);
            Timber.d("getAccessToken: ok");
            return token;
        } catch (UserRecoverableAuthException e) {
            rtIntent = e.getIntent();
            Timber.d("getAccessToken: re-try");
            return RT_CODE;
        } catch (ActivityNotFoundException e) {
            Timber.d("getAccessToken: null");
            return null;
        } catch (GoogleAuthException e) {
            Timber.d("getAccessToken: null");
            return null;
        } catch (IOException e) {
            Timber.d("getAccessToken: null");
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTHORIZATION && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            GoogleAccountManager gam = new GoogleAccountManager(activity);
            getAndUseAuthTokenInAsyncTask(gam.getAccountByName(mAccountName));
        } else if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            finishLogin();
            if (mCallback != null) mCallback.onSuccess();
        } else {
            if (mCallback != null) mCallback.onFail();
        }
    }

    private void finishLogin() {
        Prefs.getInstance(activity).setDriveUser(mAccountName);
        mGoogle = Google.getInstance(activity);
    }

    public interface LoginCallback {
        void onSuccess();

        void onFail();
    }
}
