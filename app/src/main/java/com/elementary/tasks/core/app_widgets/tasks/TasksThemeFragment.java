package com.elementary.tasks.core.app_widgets.tasks;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.databinding.FragmentTasksWidgetPreviewBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

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
public class TasksThemeFragment extends Fragment {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String ARGUMENT_DATA = "arg_data";
    private int mPageNumber;
    private List<TasksTheme> mList;

    public static TasksThemeFragment newInstance(int page, List<TasksTheme> list) {
        TasksThemeFragment pageFragment = new TasksThemeFragment();
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
        FragmentTasksWidgetPreviewBinding binding = FragmentTasksWidgetPreviewBinding.inflate(inflater, container, false);

        TasksTheme eventsTheme = mList.get(mPageNumber);

        int windowColor = eventsTheme.getWindowColor();
        binding.background.setBackgroundResource(windowColor);
        int windowTextColor = eventsTheme.getWindowTextColor();
        binding.themeTitle.setTextColor(windowTextColor);
        binding.themeTip.setTextColor(windowTextColor);

        int headerColor = eventsTheme.getHeaderColor();
        int backgroundColor = eventsTheme.getBackgroundColor();
        int titleColor = eventsTheme.getTitleColor();
        int itemTextColor = eventsTheme.getItemTextColor();

        int settingsIcon = eventsTheme.getSettingsIcon();
        int plusIcon = eventsTheme.getPlusIcon();

        binding.widgetTitle.setTextColor(titleColor);
        binding.task.setTextColor(itemTextColor);
        binding.note.setTextColor(itemTextColor);
        binding.taskDate.setTextColor(itemTextColor);

        binding.headerBg.setBackgroundResource(headerColor);
        binding.widgetBg.setBackgroundResource(backgroundColor);

        binding.tasksCount.setImageResource(plusIcon);
        binding.optionsButton.setImageResource(settingsIcon);

        binding.themeTitle.setText(eventsTheme.getTitle());
        return binding.getRoot();
    }
}
