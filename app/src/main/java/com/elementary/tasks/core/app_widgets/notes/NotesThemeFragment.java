package com.elementary.tasks.core.app_widgets.notes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.databinding.FragmentNoteWidgetPreviewBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2015 Nazar Suhovich
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

public class NotesThemeFragment extends Fragment {

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private List<NotesTheme> mList;

    public static NotesThemeFragment newInstance(int page, List<NotesTheme> list) {
        NotesThemeFragment pageFragment = new NotesThemeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putParcelableArrayList(ARGUMENT_DATA, new ArrayList<>(list));
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle intent = getArguments();
        mPageNumber = intent.getInt(ARGUMENT_PAGE_NUMBER);
        mList = intent.getParcelableArrayList(ARGUMENT_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentNoteWidgetPreviewBinding binding = FragmentNoteWidgetPreviewBinding.inflate(inflater, container, false);
        NotesTheme calendarTheme = mList.get(mPageNumber);

        int windowColor = calendarTheme.getWindowColor();
        binding.background.setBackgroundResource(windowColor);
        int windowTextColor = calendarTheme.getWindowTextColor();
        binding.themeTitle.setTextColor(windowTextColor);
        binding.themeTip.setTextColor(windowTextColor);

        int headerColor = calendarTheme.getHeaderColor();
        int backgroundColor = calendarTheme.getBackgroundColor();
        int titleColor = calendarTheme.getTitleColor();

        int settingsIcon = calendarTheme.getSettingsIcon();
        int plusIcon = calendarTheme.getPlusIcon();

        binding.widgetTitle.setTextColor(titleColor);
        binding.headerBg.setBackgroundResource(headerColor);
        binding.widgetBg.setBackgroundResource(backgroundColor);

        binding.tasksCount.setImageResource(plusIcon);
        binding.settingsButton.setImageResource(settingsIcon);

        binding.themeTitle.setText(calendarTheme.getTitle());
        return binding.getRoot();
    }
}
