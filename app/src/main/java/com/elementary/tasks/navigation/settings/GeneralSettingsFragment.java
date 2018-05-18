package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentSettingsGeneralBinding;
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.theme.SelectThemeActivity;

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
        init24TimePrefs();
        initSavePrefs();
        initLanguagePrefs();
        return binding.getRoot();
    }

    private void initLanguagePrefs() {
        binding.languagePrefs.setOnClickListener(v -> showLanguageDialog());
        showLanguage();
    }

    private void showLanguage() {
        binding.languagePrefs.setDetailText(Language.getScreenLocaleName(getContext()));
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.application_language));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, getResources().getStringArray(R.array.app_languages));
        int init = getPrefs().getAppLanguage();
        mItemSelect = init;
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setAppLanguage(mItemSelect);
            dialog.dismiss();
            if (init != mItemSelect) restartApp();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void initSavePrefs() {
        binding.savePrefs.setChecked(getPrefs().isAutoSaveEnabled());
        binding.savePrefs.setOnClickListener(v -> changeSavePrefs());
    }

    private void changeSavePrefs() {
        boolean b = binding.savePrefs.isChecked();
        getPrefs().setAutoSaveEnabled(!b);
        binding.savePrefs.setChecked(!b);
    }

    private void init24TimePrefs() {
        binding.time24hourPrefs.setChecked(getPrefs().is24HourFormatEnabled());
        binding.time24hourPrefs.setOnClickListener(view -> change24Prefs());
    }

    private void change24Prefs() {
        boolean is24 = binding.time24hourPrefs.isChecked();
        getPrefs().set24HourFormatEnabled(!is24);
        binding.time24hourPrefs.setChecked(!is24);
    }

    private void initAppTheme() {
        binding.appThemePrefs.setDetailText(getCurrentTheme());
        binding.appThemePrefs.setOnClickListener(view -> showThemeDialog());
    }

    private String getCurrentTheme() {
        int theme = getPrefs().getAppTheme();
        if (theme == ThemeUtil.THEME_AUTO) return getString(R.string.auto);
        else if (theme == ThemeUtil.THEME_WHITE) return getString(R.string.light);
        else if (theme == ThemeUtil.THEME_AMOLED) return getString(R.string.amoled);
        else return getString(R.string.dark);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.general));
            getCallback().onFragmentSelect(this);
        }
    }

    private void showThemeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.theme));
        String[] colors = new String[]{getString(R.string.auto), getString(R.string.light), getString(R.string.dark), getString(R.string.amoled)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_single_choice, colors);
        int initTheme = getPrefs().getAppTheme();
        mItemSelect = initTheme;
        builder.setSingleChoiceItems(adapter, mItemSelect, (dialog, which) -> mItemSelect = which);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            getPrefs().setAppTheme(mItemSelect);
            dialog.dismiss();
            if (initTheme != mItemSelect) restartApp();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void restartApp() {
        startActivity(new Intent(getContext(), SplashScreen.class));
        getActivity().finishAffinity();
    }

    private void selectMainImage() {
        startActivity(new Intent(getContext(), MainImageActivity.class));
    }

    private void initMainImage() {
        binding.mainImagePrefs.setOnClickListener(mMainImageClick);
    }

    private void selectTheme() {
        startActivity(new Intent(getContext(), SelectThemeActivity.class));
    }

    private void initThemeColor() {
        binding.themePrefs.setViewResource(ThemeUtil.getInstance(getContext()).getIndicator(getPrefs().getAppThemeColor()));
        binding.themePrefs.setOnClickListener(mThemeClick);
    }

    private void initSmartFold() {
        binding.smartFoldPrefs.setChecked(getPrefs().isFoldingEnabled());
        binding.smartFoldPrefs.setOnClickListener(mFoldingClick);
    }

    private void initWearNotification() {
        binding.wearPrefs.setChecked(getPrefs().isWearEnabled());
        binding.wearPrefs.setOnClickListener(mWearClick);
    }

    private void changeWearNotification() {
        boolean isChecked = binding.wearPrefs.isChecked();
        getPrefs().setWearEnabled(!isChecked);
        binding.wearPrefs.setChecked(!isChecked);
    }

    private void changeSmartFoldMode() {
        boolean isChecked = binding.smartFoldPrefs.isChecked();
        getPrefs().setFoldingEnabled(!isChecked);
        binding.smartFoldPrefs.setChecked(!isChecked);
    }
}
