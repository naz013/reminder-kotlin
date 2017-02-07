package com.elementary.tasks.navigation.settings.export;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.DropboxLogin;
import com.elementary.tasks.core.cloud.GoogleLogin;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.FragmentCloudDrivesBinding;
import com.elementary.tasks.google_tasks.GetTaskListAsync;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

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

    private DropboxLogin mDropbox;
    private GoogleLogin mGoogleLogin;

    private FragmentCloudDrivesBinding binding;
    private RoboButton mDropboxButton, mGoogleDriveButton;
    private RoboTextView mGoogleDriveTitle, mDropboxTitle;

    private ProgressDialog mDialog;
    private GoogleLogin.LoginCallback mLoginCallback = new GoogleLogin.LoginCallback() {
        @Override
        public void onSuccess() {
            startSync();
        }

        @Override
        public void onFail() {
            showErrorDialog();
        }
    };
    private DropboxLogin.LoginCallback mDropboxCallback = new DropboxLogin.LoginCallback() {
        @Override
        public void onSuccess(boolean logged) {
            if (logged) {
                mDropboxButton.setText(getString(R.string.disconnect));
            } else {
                mDropboxButton.setText(getString(R.string.connect));
            }
        }
    };

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(getString(R.string.failed_to_login));
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCloudDrivesBinding.inflate(inflater, container, false);
        mDropbox = new DropboxLogin(getActivity(), mDropboxCallback);
        mGoogleLogin = new GoogleLogin(getActivity(), mLoginCallback);
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
        mGoogleDriveButton.setOnClickListener(v -> googleDriveButtonClick());
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
        mDropboxButton.setOnClickListener(v -> mDropbox.login());
    }

    private void switchGoogleStatus() {
        if (!SuperUtil.checkGooglePlayServicesAvailability(getActivity())) {
            Toast.makeText(mContext, R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mGoogleLogin.isLogged()){
            disconnectFromGoogleServices();
        } else {
            mGoogleLogin.login();
        }
    }

    private void disconnectFromGoogleServices() {
        mGoogleLogin.logOut();
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

    private void checkGoogleStatus(){
        if (mGoogleLogin.isLogged()){
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
        mDropbox.checkDropboxStatus();
        checkGoogleStatus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mGoogleLogin != null) mGoogleLogin.onActivityResult(requestCode, resultCode, data);
    }

    private void startSync() {
        checkGoogleStatus();
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
