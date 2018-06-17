package com.elementary.tasks.google_tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.view_models.google_tasks.GoogleTaskListViewModel;
import com.elementary.tasks.databinding.FragmentGoogleListBinding;
import com.elementary.tasks.navigation.fragments.BaseFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

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

public class TaskListFragment extends BaseFragment {

    private static final String ARG_ID = "arg_id";

    private FragmentGoogleListBinding binding;
    @NonNull
    private TasksRecyclerAdapter adapter = new TasksRecyclerAdapter();
    private GoogleTaskListViewModel viewModel;
    @Nullable
    private String mId;

    public static TaskListFragment newInstance(@Nullable String id) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ID, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getString(ARG_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGoogleListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initEmpty();
        initList();
        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this, new GoogleTaskListViewModel.Factory(getActivity().getApplication(), mId)).get(GoogleTaskListViewModel.class);
        viewModel.googleTasks.observe(this, googleTasks -> {
            if (googleTasks != null) {
                showTasks(googleTasks);
            }
        });
    }

    private void showTasks(List<GoogleTask> googleTasks) {
        adapter.setGoogleTasks(googleTasks);
        reloadView();
    }

    private void initList() {
        adapter.setActionsListener((view, position, googleTask, actions) -> {
            switch (actions) {
                case EDIT:
                    if (googleTask != null) editTask(googleTask);
                    break;
                case SWITCH:
                    if (googleTask != null) viewModel.toggleTask(googleTask);
                    break;
            }
        });
    }

    private void editTask(GoogleTask googleTask) {
        startActivity(new Intent(getActivity(), TaskActivity.class)
                .putExtra(Constants.INTENT_ID, googleTask.getTaskId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
    }

    private void initEmpty() {
        binding.emptyItem.setVisibility(View.VISIBLE);
        binding.emptyText.setText(R.string.no_google_tasks);
        reloadView();
    }

    private void reloadView() {
        if (adapter.getItemCount() > 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }
}
