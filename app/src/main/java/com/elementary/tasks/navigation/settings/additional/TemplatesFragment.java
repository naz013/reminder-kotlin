package com.elementary.tasks.navigation.settings.additional;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.FragmentTemplatesListBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.reminder.filters.FilterCallback;

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
public class TemplatesFragment extends BaseSettingsFragment implements FilterCallback<TemplateItem> {

    private FragmentTemplatesListBinding binding;
    private TemplatesAdapter adapter;

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    @NonNull
    private TemplateFilterController filterController = new TemplateFilterController(this);

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (adapter != null) filterController.setSearchValue(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (adapter != null) filterController.setSearchValue(newText);
            return false;
        }
    };

    private SearchView.OnCloseListener mCloseListener = () -> {
        showTemplates();
        return true;
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.templates_menu, menu);
        mSearchMenu = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (mSearchMenu != null) {
            mSearchView = (SearchView) mSearchMenu.getActionView();
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            }
            mSearchView.setOnQueryTextListener(queryTextListener);
            mSearchView.setOnCloseListener(mCloseListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTemplatesListBinding.inflate(inflater, container, false);
        initTemplateList();
        return binding.getRoot();
    }

    private void openCreateScreen() {
        startActivity(new Intent(getContext(), TemplateActivity.class));
    }

    private void initTemplateList() {
        binding.templatesList.setHasFixedSize(false);
        binding.templatesList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TemplatesAdapter(getContext());
        binding.templatesList.setAdapter(adapter);
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.messages));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> openCreateScreen());
            getCallback().onScrollChanged(binding.templatesList);
        }
        showTemplates();
    }

    private void showTemplates() {
        filterController.setOriginal(RealmDb.getInstance().getAllTemplates());
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

    @Override
    public void onChanged(@NonNull List<TemplateItem> result) {
        adapter.setData(result);
        binding.templatesList.smoothScrollToPosition(0);
        refreshView();
    }
}
