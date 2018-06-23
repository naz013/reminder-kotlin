package com.elementary.tasks.navigation.settings.export;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.DropboxLogin;
import com.elementary.tasks.core.cloud.GoogleLogin;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.databinding.FragmentCloudDrivesBinding;
import com.elementary.tasks.google_tasks.GetTaskListAsync;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private ProgressDialog mDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper());
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
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setMessage(getString(R.string.failed_to_login));
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCloudDrivesBinding.inflate(inflater, container, false);
        mDropbox = new DropboxLogin(getActivity(), mDropboxCallback);
        mGoogleLogin = new GoogleLogin(getActivity(), mLoginCallback);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDropboxButton();
        initGoogleDriveButton();
        checkGoogleStatus();
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
            Toast.makeText(getContext(), R.string.google_play_services_not_installed, Toast.LENGTH_SHORT).show();
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
        new Thread(() -> {
            AppDb.getAppDatabase(getContext()).googleTasksDao().deleteAll();
            AppDb.getAppDatabase(getContext()).googleTaskListsDao().deleteAll();
            mHandler.post(this::finishSync);
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch(requestCode){
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    switchGoogleStatus();
                }
                break;
        }
    }

    private void checkGoogleStatus() {
        if (mGoogleLogin.isLogged()) {
            mGoogleDriveButton.setText(R.string.disconnect);
        } else {
            mGoogleDriveButton.setText(getString(R.string.connect));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.cloud_services));
            getCallback().onFragmentSelect(this);
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
        mDialog = ProgressDialog.show(getContext(), null, getString(R.string.retrieving_tasks), false, true);
        new GetTaskListAsync(getContext(), new TasksCallback() {
            @Override
            public void onFailed() {
                finishSync();
            }

            @Override
            public void onComplete() {
                finishSync();
            }
        }).execute();
    }

    private void finishSync() {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
        checkGoogleStatus();
        if (getCallback() != null) getCallback().refreshMenu();
    }
}
