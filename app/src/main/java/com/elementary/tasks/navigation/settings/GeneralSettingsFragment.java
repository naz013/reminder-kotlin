package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.services.GcmListenerService;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding;
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.theme.SelectThemeActivity;
import com.google.firebase.messaging.FirebaseMessaging;

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

public class GeneralSettingsFragment extends BaseSettingsFragment {

    private FragmentSettingsGeneralBinding binding;
    private View.OnClickListener mFoldingClick = view -> changeSmartFoldMode();
    private View.OnClickListener mWearClick = view -> changeWearNotification();
    private View.OnClickListener mThemeClick = view -> selectTheme();
    private View.OnClickListener mMainImageClick = view -> selectMainImage();

    private int mItemSelect;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false);
        initAppTheme();
        initThemeColor();
        initMainImage();
        initSmartFold();
        initWearNotification();
        initGcmPrefs();
        return binding.getRoot();
    }

    private void initGcmPrefs() {
        binding.gcmPrefs.setChecked(Prefs.getInstance(mContext).isGcmEnabled());
        binding.gcmPrefs.setOnClickListener(view -> changeGcmPrefs());
    }

    private void changeGcmPrefs() {
        boolean isChecked = binding.gcmPrefs.isChecked();
        Prefs.getInstance(mContext).setGcmEnabled(!isChecked);
        binding.gcmPrefs.setChecked(!isChecked);
        if (!isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic(GcmListenerService.TOPIC_NAME);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(GcmListenerService.TOPIC_NAME);
        }
    }

    private void initAppTheme() {
        binding.appThemePrefs.setDetailText(getCurrentTheme());
        binding.appThemePrefs.setOnClickListener(view -> showThemeDialog());
    }

    private String getCurrentTheme() {
        int theme = Prefs.getInstance(mContext).getAppTheme();
        if (theme == ThemeUtil.THEME_AUTO) return getString(R.string.auto);
        else if (theme == ThemeUtil.THEME_WHITE) return getString(R.string.light);
        else return getString(R.string.dark);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.general));
            mCallback.onFragmentSelect(this);
        }
    }

    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.theme));
        String[] colors = new String[]{getString(R.string.auto), getString(R.string.light), getString(R.string.dark)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, colors);
        int initTheme = Prefs.getInstance(mContext).getAppTheme();
        mItemSelect = initTheme;
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> {
            mItemSelect = which;
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            Prefs.getInstance(mContext).setAppTheme(mItemSelect);
            dialog.dismiss();
            if (initTheme != mItemSelect) restartApp();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void restartApp() {
        startActivity(new Intent(mContext, SplashScreen.class));
        getActivity().finish();
    }

    private void selectMainImage() {
        startActivity(new Intent(mContext, MainImageActivity.class));
    }

    private void initMainImage() {
        binding.mainImagePrefs.setOnClickListener(mMainImageClick);
    }

    private void selectTheme() {
        startActivity(new Intent(mContext, SelectThemeActivity.class));
    }

    private void initThemeColor() {
        binding.themePrefs.setViewResource(ThemeUtil.getInstance(mContext).getIndicator(Prefs.getInstance(mContext).getAppThemeColor()));
        binding.themePrefs.setOnClickListener(mThemeClick);
    }

    private void initSmartFold() {
        binding.smartFoldPrefs.setChecked(Prefs.getInstance(mContext).isFoldingEnabled());
        binding.smartFoldPrefs.setOnClickListener(mFoldingClick);
    }

    private void initWearNotification() {
        binding.wearPrefs.setChecked(Prefs.getInstance(mContext).isWearEnabled());
        binding.wearPrefs.setOnClickListener(mWearClick);
    }

    private void changeWearNotification() {
        boolean isChecked = binding.wearPrefs.isChecked();
        Prefs.getInstance(mContext).setWearEnabled(!isChecked);
        binding.wearPrefs.setChecked(!isChecked);
    }

    private void changeSmartFoldMode() {
        boolean isChecked = binding.smartFoldPrefs.isChecked();
        Prefs.getInstance(mContext).setFoldingEnabled(!isChecked);
        binding.smartFoldPrefs.setChecked(!isChecked);
    }
}
