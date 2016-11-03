package com.elementary.tasks.navigation.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.views.PrefsView;
import com.elementary.tasks.databinding.FragmentSettingsNotificationBinding;

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

public class NotificationSettingsFragment extends BaseSettingsFragment {

    private FragmentSettingsNotificationBinding binding;

    private PrefsView mBlurPrefs;
    private PrefsView mManualPrefs;
    private PrefsView mSbPrefs;
    private PrefsView mSbIconPrefs;
    private PrefsView mVibratePrefs;
    private PrefsView mInfiniteVibratePrefs;

    private View.OnClickListener mImageClick = view -> showImageDialog();
    private View.OnClickListener mBlurClick = view -> changeBlurPrefs();
    private View.OnClickListener mManualClick = view -> changeManualPrefs();
    private View.OnClickListener mSbClick = view -> changeSbPrefs();
    private View.OnClickListener mSbIconClick = view -> changeSbIconPrefs();
    private View.OnClickListener mVibrateClick = view -> changeVibratePrefs();
    private View.OnClickListener mInfiniteVibrateClick = view -> changeInfiniteVibratePrefs();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsNotificationBinding.inflate(inflater, container, false);
        binding.imagePrefs.setOnClickListener(mImageClick);
        initBlurPrefs();
        initManualPrefs();
        initSbPrefs();
        initSbIconPrefs();
        initVibratePrefs();
        initInfiniteVibratePrefs();
        return binding.getRoot();
    }

    private void changeInfiniteVibratePrefs() {
        boolean isChecked = mInfiniteVibratePrefs.isChecked();
        mInfiniteVibratePrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setInfiniteVibrateEnabled(!isChecked);
    }

    private void initInfiniteVibratePrefs() {
        mInfiniteVibratePrefs = binding.infiniteVibrateOptionPrefs;
        mInfiniteVibratePrefs.setOnClickListener(mInfiniteVibrateClick);
        mInfiniteVibratePrefs.setChecked(Prefs.getInstance(mContext).isInfiniteVibrateEnabled());
        checkVibrateEnabling();
    }

    private void changeVibratePrefs() {
        boolean isChecked = mVibratePrefs.isChecked();
        mVibratePrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setVibrateEnabled(!isChecked);
        checkVibrateEnabling();
    }

    private void checkVibrateEnabling() {
        mInfiniteVibratePrefs.setEnabled(mVibratePrefs.isChecked());
    }

    private void initVibratePrefs() {
        mVibratePrefs = binding.vibrationOptionPrefs;
        mVibratePrefs.setOnClickListener(mVibrateClick);
        mVibratePrefs.setChecked(Prefs.getInstance(mContext).isVibrateEnabled());
    }

    private void changeSbIconPrefs() {
        boolean isChecked = mSbIconPrefs.isChecked();
        mSbIconPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSbIconEnabled(!isChecked);
        new Notifier(getActivity()).recreatePermanent();
    }

    private void initSbIconPrefs() {
        mSbIconPrefs = binding.statusIconPrefs;
        mSbIconPrefs.setOnClickListener(mSbIconClick);
        checkSbIconEnabling();
    }

    private void changeSbPrefs() {
        boolean isChecked = mSbPrefs.isChecked();
        mSbPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setSbNotificationEnabled(!isChecked);
        checkSbIconEnabling();
        if (Prefs.getInstance(mContext).isSbNotificationEnabled()) {
            new Notifier(mContext).showPermanent();
        } else {
            new Notifier(mContext).hidePermanent();
        }
    }

    private void checkSbIconEnabling() {
        mSbIconPrefs.setEnabled(mSbPrefs.isChecked());
    }

    private void initSbPrefs() {
        mSbPrefs = binding.permanentNotificationPrefs;
        mSbPrefs.setOnClickListener(mSbClick);
        mSbPrefs.setChecked(Prefs.getInstance(mContext).isSbNotificationEnabled());
    }

    private void changeManualPrefs() {
        boolean isChecked = mManualPrefs.isChecked();
        mManualPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setManualRemoveEnabled(!isChecked);
    }

    private void initManualPrefs() {
        mManualPrefs = binding.notificationDismissPrefs;
        mManualPrefs.setOnClickListener(mManualClick);
        mManualPrefs.setChecked(Prefs.getInstance(mContext).isManualRemoveEnabled());
    }

    private void initBlurPrefs() {
        mBlurPrefs = binding.blurPrefs;
        mBlurPrefs.setOnClickListener(mBlurClick);
        mBlurPrefs.setChecked(Prefs.getInstance(mContext).isBlurEnabled());
    }

    private void changeBlurPrefs() {
        boolean isChecked = mBlurPrefs.isChecked();
        mBlurPrefs.setChecked(!isChecked);
        Prefs.getInstance(mContext).setBlurEnabled(!isChecked);
    }

    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.background));
        String[] types = new String[]{mContext.getString(R.string.none),
                mContext.getString(R.string.default_string),
                mContext.getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, types);
        String image = Prefs.getInstance(mContext).getReminderImage();
        int selection;
        if (image.matches(Constants.NONE)) {
            selection = 0;
        } else if (image.matches(Constants.DEFAULT)) {
            selection = 1;
        } else {
            selection = 2;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                saveImagePrefs(which);
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            saveImagePrefs(which);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveImagePrefs(int which) {
        Prefs prefs = Prefs.getInstance(mContext);
        if (which == 0) {
            prefs.setReminderImage(Constants.NONE);
        } else if (which == 1) {
            prefs.setReminderImage(Constants.DEFAULT);
        } else if (which == 2) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if (Module.isKitkat()) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
            }
            Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.image));
            startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.notification));
            mCallback.onFragmentSelect(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ACTION_REQUEST_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Prefs.getInstance(mContext).setReminderImage(selectedImage.toString());
                }
                break;
        }
    }
}
