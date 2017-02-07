package com.elementary.tasks.google_tasks;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.FragmentGoogleListBinding;

import java.util.List;
import java.util.Map;

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
public class TaskListFragment extends Fragment {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private List<TaskItem> data;
    private Map<String, Integer> colors;
    private TasksCallback mTasksCallback = new TasksCallback() {
        @Override
        public void onFailed() {

        }

        @Override
        public void onComplete() {
            loaderAdapter();
        }
    };

    public void setData(List<TaskItem> data, Map<String, Integer> colors) {
        this.data = data;
        this.colors = colors;
    }

    public static TaskListFragment newInstance() {
        return new TaskListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentGoogleListBinding binding = FragmentGoogleListBinding.inflate(inflater, container, false);
        emptyItem = binding.emptyItem;
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = binding.emptyText;
        emptyText.setText(R.string.no_google_tasks);
        emptyItem.setVisibility(View.VISIBLE);
        currentList = binding.recyclerView;
        loaderAdapter();
        return binding.getRoot();
    }

    public void loaderAdapter() {
        TasksRecyclerAdapter customAdapter = new TasksRecyclerAdapter(getActivity(), data, colors);
        customAdapter.setListener(mTasksCallback);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        currentList.setAdapter(customAdapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        reloadView();
    }

    private void reloadView() {
        if (data != null && data.size() > 0) {
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }
}
