package com.elementary.tasks.navigation.settings.additional;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.FragmentTemplatesListBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

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

public class TemplatesFragment extends BaseSettingsFragment {

    private FragmentTemplatesListBinding binding;
    private TemplatesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTemplatesListBinding.inflate(inflater, container, false);
        binding.fab.setOnClickListener(view -> openCreateScreen());
        initTemplateList();
        return binding.getRoot();
    }

    private void openCreateScreen() {
        startActivity(new Intent(mContext, TemplateActivity.class));
    }

    private void initTemplateList() {
        RecyclerView recyclerView = binding.templatesList;
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.messages));
            mCallback.onFragmentSelect(this);
        }
        showTemplates();
    }

    private void showTemplates() {
        adapter = new TemplatesAdapter(RealmDb.getInstance().getAllTemplates(), mContext);
        binding.templatesList.setAdapter(adapter);
        refreshView();
    }

    private void refreshView() {
        if (adapter == null || adapter.getItemCount() == 0) {
            binding.emptyItem.setVisibility(View.VISIBLE);
            binding.templatesList.setVisibility(View.GONE);
        } else {
            binding.emptyItem.setVisibility(View.GONE);
            binding.templatesList.setVisibility(View.VISIBLE);
        }
    }
}
