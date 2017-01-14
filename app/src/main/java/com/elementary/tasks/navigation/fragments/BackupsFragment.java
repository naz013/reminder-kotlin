package com.elementary.tasks.navigation.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.backups.DeleteAsync;
import com.elementary.tasks.backups.InfoAdapter;
import com.elementary.tasks.backups.UserInfoAsync;
import com.elementary.tasks.backups.UserItem;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.GoogleDrive;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.databinding.FragmentBackupsBinding;

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

    private FragmentBackupsBinding binding;
    private InfoAdapter mAdapter;
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

    private void deleteFiles(UserInfoAsync.Info info) {
        new DeleteAsync(mContext, mDeleteCallback, info).execute(getFolders(info));
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
        binding = FragmentBackupsBinding.inflate(inflater, container, false);
        mAdapter = new InfoAdapter(binding.itemsContainer, mContext, mActionCallback);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.backup_files));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(null);
        }
    }

    private String[] getFolders(UserInfoAsync.Info info) {
        if (info == UserInfoAsync.Info.Dropbox) {
            String r = MemoryUtil.getDropboxRemindersDir().getPath();
            String n = MemoryUtil.getDropboxNotesDir().getPath();
            String g = MemoryUtil.getDropboxGroupsDir().getPath();
            String b = MemoryUtil.getDropboxBirthdaysDir().getPath();
            return new String[]{r, n, g, b};
        } else if (info == UserInfoAsync.Info.Google) {
            String r = MemoryUtil.getGoogleRemindersDir().getPath();
            String n = MemoryUtil.getGoogleNotesDir().getPath();
            String g = MemoryUtil.getGoogleGroupsDir().getPath();
            String b = MemoryUtil.getGoogleBirthdaysDir().getPath();
            return new String[]{r, n, g, b};
        } else {
            String r = MemoryUtil.getRemindersDir().getPath();
            String n = MemoryUtil.getNotesDir().getPath();
            String g = MemoryUtil.getGroupsDir().getPath();
            String b = MemoryUtil.getBirthdaysDir().getPath();
            return new String[]{r, n, g, b};
        }
    }

    private void loadUserInfo() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(getActivity(), SD_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            return;
        }
        List<UserInfoAsync.Info> list = new ArrayList<>();
        list.add(UserInfoAsync.Info.Local);
        Dropbox dbx = new Dropbox(mContext);
        dbx.startSession();
        if (dbx.isLinked()){
            list.add(UserInfoAsync.Info.Dropbox);
        }
        GoogleDrive gdx = new GoogleDrive(mContext);
        if (gdx.isLinked()) {
            list.add(UserInfoAsync.Info.Google);
        }
        UserInfoAsync.Info[] array = new UserInfoAsync.Info[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        new UserInfoAsync(mContext, mDataCallback, list.size()).execute(array);
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
