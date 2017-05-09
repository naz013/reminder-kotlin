package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.DialogAboutLayoutBinding;
import com.elementary.tasks.databinding.FragmentSettingsOtherBinding;
import com.elementary.tasks.navigation.settings.other.ChangesFragment;
import com.elementary.tasks.navigation.settings.other.OssFragment;
import com.elementary.tasks.navigation.settings.other.PermissionsFragment;

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

public class OtherSettingsFragment extends BaseSettingsFragment {

    private List<Item> mDataList = new ArrayList<>();

    private View.OnClickListener mAboutClick = view -> showAboutDialog();
    private View.OnClickListener mOssClick = view -> openOssScreen();
    private View.OnClickListener mPermissionsClick = view -> openPermissionsScreen();
    private View.OnClickListener mChangesClick = view -> openChangesScreen();
    private View.OnClickListener mRateClick = view -> SuperUtil.launchMarket(getContext());
    private View.OnClickListener mShareClick = view -> shareApplication();
    private View.OnClickListener mAddClick = view -> showPermissionDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentSettingsOtherBinding binding = FragmentSettingsOtherBinding.inflate(inflater, container, false);
        binding.aboutPrefs.setOnClickListener(mAboutClick);
        binding.ossPrefs.setOnClickListener(mOssClick);
        binding.permissionsPrefs.setOnClickListener(mPermissionsClick);
        binding.changesPrefs.setOnClickListener(mChangesClick);
        binding.ratePrefs.setOnClickListener(mRateClick);
        binding.tellFriendsPrefs.setOnClickListener(mShareClick);
        if (Module.isMarshmallow()) {
            binding.permissionsPrefs.setVisibility(View.VISIBLE);
            binding.addPermissionPrefs.setVisibility(View.VISIBLE);
        } else {
            binding.permissionsPrefs.setVisibility(View.GONE);
            binding.addPermissionPrefs.setVisibility(View.GONE);
        }
        binding.addPermissionPrefs.setOnClickListener(mAddClick);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.other));
            getCallback().onFragmentSelect(this);
        }
    }

    private void requestPermission(int position) {
        Permissions.requestPermission(getActivity(), position, mDataList.get(position).getPermission());
    }

    private boolean loadDataToList(){
        mDataList.clear();
        if (!Permissions.checkPermission(getActivity(), Permissions.ACCESS_COARSE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.course_location), Permissions.ACCESS_COARSE_LOCATION));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.ACCESS_FINE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.CALL_PHONE)) {
            mDataList.add(new Item(getString(R.string.call_phone), Permissions.CALL_PHONE));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.GET_ACCOUNTS)) {
            mDataList.add(new Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_PHONE_STATE)) {
            mDataList.add(new Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR)) {
            mDataList.add(new Item(getString(R.string.read_calendar), Permissions.READ_CALENDAR));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.WRITE_CALENDAR)) {
            mDataList.add(new Item(getString(R.string.write_calendar), Permissions.WRITE_CALENDAR));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
            mDataList.add(new Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CALLS)) {
            mDataList.add(new Item(getString(R.string.call_history), Permissions.READ_CALLS));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.WRITE_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL));
        }
        if (!Permissions.checkPermission(getActivity(), Permissions.SEND_SMS)) {
            mDataList.add(new Item(getString(R.string.send_sms), Permissions.SEND_SMS));
        }
        if (mDataList.size() == 0) {
            Toast.makeText(getContext(), R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void shareApplication() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
        getContext().startActivity(Intent.createChooser(shareIntent, "Share..."));
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

    private void showPermissionDialog() {
        if (!loadDataToList()) return;
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(R.string.allow_permission);
        builder.setSingleChoiceItems(new ArrayAdapter<Item>(getContext(), android.R.layout.simple_list_item_1) {
            @Override
            public int getCount() {
                return mDataList.size();
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                TextView tvName = (TextView) convertView.findViewById(android.R.id.text1);
                tvName.setText(mDataList.get(position).getTitle());
                return convertView;
            }
        }, -1, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            requestPermission(i);
        });
        builder.create().show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        DialogAboutLayoutBinding binding = DialogAboutLayoutBinding.inflate(LayoutInflater.from(getContext()));
        String name;
        if (Module.isPro()) name = getString(R.string.app_name_pro);
        else name = getString(R.string.app_name);
        binding.appName.setText(name.toUpperCase());
        PackageInfo pInfo;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            binding.appVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setView(binding.getRoot());
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showPermissionDialog();
        }
    }

    static class Item {
        private String title, permission;

        Item(String title, String permission) {
            this.permission = permission;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public String getPermission() {
            return permission;
        }
    }
}
