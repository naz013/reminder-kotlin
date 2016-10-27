package com.elementary.tasks.navigation.settings;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.databinding.DialogAboutLayoutBinding;
import com.elementary.tasks.databinding.FragmentSettingsOtherBinding;
import com.elementary.tasks.navigation.settings.other.ChangesFragment;
import com.elementary.tasks.navigation.settings.other.OssFragment;
import com.elementary.tasks.navigation.settings.other.PermissionsFragment;

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

public class OtherSettingsFragment extends BaseSettingsFragment {

    private View.OnClickListener mAboutClick = view -> showAboutDialog();
    private View.OnClickListener mOssClick = view -> openOssScreen();
    private View.OnClickListener mPermissionsClick = view -> openPermissionsScreen();
    private View.OnClickListener mChangesClick = view -> openChangesScreen();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsOtherBinding binding = FragmentSettingsOtherBinding.inflate(inflater, container, false);
        binding.aboutPrefs.setOnClickListener(mAboutClick);
        binding.ossPrefs.setOnClickListener(mOssClick);
        binding.permissionsPrefs.setOnClickListener(mPermissionsClick);
        binding.changesPrefs.setOnClickListener(mChangesClick);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.other));
            mCallback.onFragmentSelect(this);
        }
    }

    private void openChangesScreen() {
        replaceFragment(new ChangesFragment(), getString(R.string.changes));
    }

    private void openPermissionsScreen() {
        replaceFragment(new PermissionsFragment(), getString(R.string.permissions));
    }

    private void openOssScreen() {
        replaceFragment(new OssFragment(), getString(R.string.open_source_licenses));
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        DialogAboutLayoutBinding binding = DialogAboutLayoutBinding.inflate(LayoutInflater.from(mContext));
        String name;
        if (Module.isPro()) name = getString(R.string.app_name_pro);
        else name = getString(R.string.app_name);
        binding.appName.setText(name.toUpperCase());
        PackageInfo pInfo;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            binding.appVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setView(binding.getRoot());
        builder.create().show();
    }
}
