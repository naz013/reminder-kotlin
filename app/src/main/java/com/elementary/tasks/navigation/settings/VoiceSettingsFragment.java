package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.FragmentSettingsVoiceBinding;
import com.elementary.tasks.navigation.settings.voice.HelpFragment;
import com.elementary.tasks.navigation.settings.voice.TimeOfDayFragment;

import java.util.ArrayList;

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

public class VoiceSettingsFragment extends BaseSettingsFragment {

    private FragmentSettingsVoiceBinding binding;

    private View.OnClickListener mVoiceClick = view -> showLanguageDialog();
    private View.OnClickListener mTimeClick = view -> replaceFragment(new TimeOfDayFragment(), getString(R.string.time));
    private View.OnClickListener mHelpClick = view -> replaceFragment(new HelpFragment(), getString(R.string.help));

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsVoiceBinding.inflate(inflater, container, false);
        binding.languagePrefs.setOnClickListener(mVoiceClick);
        binding.timePrefs.setOnClickListener(mTimeClick);
        binding.helpPrefs.setOnClickListener(mHelpClick);
        binding.conversationPrefs.setOnClickListener(view -> changeLivePrefs());
        binding.conversationPrefs.setChecked(Prefs.getInstance(mContext).isLiveEnabled());
        return binding.getRoot();
    }

    private void changeLivePrefs() {
        boolean isChecked = binding.conversationPrefs.isChecked();
        Prefs.getInstance(mContext).setLiveEnabled(!isChecked);
        binding.conversationPrefs.setChecked(!isChecked);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.voice_control));
            mCallback.onFragmentSelect(this);
        }
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(mContext.getString(R.string.language));
        final ArrayList<String> locales = Language.getLanguages(mContext);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_single_choice, locales);
        int language = Prefs.getInstance(mContext).getVoiceLocale();
        builder.setSingleChoiceItems(adapter, language, (dialog, which) -> {
            if (which != -1) {
                Prefs.getInstance(mContext).setVoiceLocale(which);
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
