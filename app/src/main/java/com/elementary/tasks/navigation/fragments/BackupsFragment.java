package com.elementary.tasks.navigation.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.backups.DeleteAsync;
import com.elementary.tasks.backups.InfoAdapter;
import com.elementary.tasks.backups.UserInfoAsync;
import com.elementary.tasks.backups.UserItem;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.databinding.FragmentBackupsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class BackupsFragment extends BaseNavigationFragment {

    private static final int SD_CODE = 623;
    private static final String TAG = "BackupsFragment";

    private InfoAdapter mAdapter;
    private UserInfoAsync mTask;
    private UserInfoAsync.DataListener mDataCallback = new UserInfoAsync.DataListener() {
        @Override
        public void onReceive(List<UserItem> result) {
            if (mAdapter != null) {
                mAdapter.setData(result);
            }
        }
    };
    private InfoAdapter.ActionCallback mActionCallback = this::deleteFiles;
    private DeleteAsync.DeleteCallback mDeleteCallback = this::loadUserInfo;
    private UserInfoAsync.DialogListener mCancelListener = () -> {
        Toast.makeText(getContext(), R.string.canceled, Toast.LENGTH_SHORT).show();
        cancelTask();
    };

    private void cancelTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    private void deleteFiles(UserInfoAsync.Info info) {
        new DeleteAsync(getContext(), mDeleteCallback, info).execute(getFolders(info));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                loadUserInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentBackupsBinding binding = FragmentBackupsBinding.inflate(inflater, container, false);
        mAdapter = new InfoAdapter(binding.itemsContainer, getContext(), mActionCallback);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.backup_files));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(null);
        }
    }

    private File[] getFolders(UserInfoAsync.Info info) {
        if (info == UserInfoAsync.Info.Dropbox) {
            return getDropboxFolders();
        } else if (info == UserInfoAsync.Info.Google) {
            return getGoogleFolders();
        } else {
            return getLocalFolders();
        }
    }

    @NonNull
    private File[] getLocalFolders() {
        File r = MemoryUtil.getRemindersDir();
        File n = MemoryUtil.getNotesDir();
        File g = MemoryUtil.getGroupsDir();
        File b = MemoryUtil.getBirthdaysDir();
        File p = MemoryUtil.getPlacesDir();
        File s = MemoryUtil.getPrefsDir();
        File t = MemoryUtil.getTemplatesDir();
        return new File[]{r, n, g, b, p, s, t};
    }

    @NonNull
    private File[] getGoogleFolders() {
        File r = MemoryUtil.getGoogleRemindersDir();
        File n = MemoryUtil.getGoogleNotesDir();
        File g = MemoryUtil.getGoogleGroupsDir();
        File b = MemoryUtil.getGoogleBirthdaysDir();
        File p = MemoryUtil.getGooglePlacesDir();
        File s = MemoryUtil.getGooglePrefsDir();
        File t = MemoryUtil.getGoogleTemplatesDir();
        return new File[]{r, n, g, b, p, s, t};
    }

    @NonNull
    private File[] getDropboxFolders() {
        File r = MemoryUtil.getDropboxRemindersDir();
        File n = MemoryUtil.getDropboxNotesDir();
        File g = MemoryUtil.getDropboxGroupsDir();
        File b = MemoryUtil.getDropboxBirthdaysDir();
        File p = MemoryUtil.getDropboxPlacesDir();
        File s = MemoryUtil.getDropboxPrefsDir();
        File t = MemoryUtil.getDropboxTemplatesDir();
        return new File[]{r, n, g, b, p, s, t};
    }

    private void loadUserInfo() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(getActivity(), SD_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            return;
        }
        List<UserInfoAsync.Info> list = new ArrayList<>();
        list.add(UserInfoAsync.Info.Local);
        Dropbox dbx = new Dropbox(getContext());
        dbx.startSession();
        if (dbx.isLinked()) {
            list.add(UserInfoAsync.Info.Dropbox);
        }
        Google gdx = Google.getInstance(getContext());
        if (gdx != null) {
            list.add(UserInfoAsync.Info.Google);
        }
        UserInfoAsync.Info[] array = new UserInfoAsync.Info[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        cancelTask();
        mTask = new UserInfoAsync(getContext(), mDataCallback, list.size(), mCancelListener);
        mTask.execute(array);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SD_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadUserInfo();
                }
                break;
        }
    }
}
