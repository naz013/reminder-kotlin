package com.elementary.tasks.core.cloud;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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
public class DropboxLogin {

    public static final String TAG = "DropboxLogin";
    public static final String MARKET_APP_JUSTREMINDER = "com.cray.software.justreminder";
    public static final String MARKET_APP_JUSTREMINDER_PRO = "com.cray.software.justreminderpro";

    @NonNull
    private Activity mContext;
    private Dropbox mDropbox;
    @NonNull
    private DropboxLogin.LoginCallback mCallback;

    public DropboxLogin(@NonNull Activity context, @NonNull DropboxLogin.LoginCallback callback) {
        this.mContext = context;
        this.mDropbox = new Dropbox(context);
        this.mCallback = callback;
        this.mDropbox.startSession();
    }

    public void login() {
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
                mCallback.onSuccess(false);
            }
        } else {
            mDropbox.startLink();
        }
    }

    public void checkDropboxStatus() {
        LogUtil.d(TAG,  "checkDropboxStatus: " + mDropbox.isLinked());
        if (mDropbox.isLinked()) {
            mCallback.onSuccess(true);
        } else {
            LogUtil.d(TAG,  "checkDropboxStatus2: " + mDropbox.isLinked());
            mDropbox.startSession();
            if (mDropbox.isLinked()) {
                mCallback.onSuccess(true);
            } else {
                mCallback.onSuccess(false);
            }
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

    private Dialog checkDialog() {
        return new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.other_version_detected))
                .setPositiveButton(mContext.getString(R.string.open), (dialogInterface, i) -> openApp())
                .setNegativeButton(mContext.getString(R.string.delete), (dialogInterface, i) -> deleteApp())
                .setNeutralButton(mContext.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .setCancelable(true)
                .create();
    }

    private void deleteApp() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        if (Module.isPro()) {
            intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER));
        } else {
            intent.setData(Uri.parse("package:" + MARKET_APP_JUSTREMINDER_PRO));
        }
        mContext.startActivity(intent);
    }

    private void openApp() {
        Intent i;
        PackageManager manager = mContext.getPackageManager();
        if (Module.isPro()) {
            i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER);
        } else {
            i = manager.getLaunchIntentForPackage(MARKET_APP_JUSTREMINDER_PRO);
        }
        if (i != null) {
            i.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        mContext.startActivity(i);
    }

    public interface LoginCallback {
        void onSuccess(boolean b);
    }
}
