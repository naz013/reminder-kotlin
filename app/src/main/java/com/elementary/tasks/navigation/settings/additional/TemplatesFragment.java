package com.elementary.tasks.navigation.settings.additional;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.models.SmsTemplate;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel;
import com.elementary.tasks.databinding.FragmentTemplatesListBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

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
public class TemplatesFragment extends BaseSettingsFragment implements FilterCallback<SmsTemplate> {

    private FragmentTemplatesListBinding binding;
    @NonNull
    private TemplatesAdapter adapter = new TemplatesAdapter();
    private SmsTemplatesViewModel viewModel;

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
        filterController.setSearchValue("");
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SmsTemplatesViewModel.class);
        viewModel.smsTemplates.observe(this, smsTemplates -> {
            if (smsTemplates != null) {
                showTemplates(smsTemplates);
            }
        });
    }

    private void openCreateScreen() {
        startActivity(new Intent(getContext(), TemplateActivity.class));
    }

    private void initTemplateList() {
        binding.templatesList.setHasFixedSize(false);
        binding.templatesList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setActionsListener((view, position, smsTemplate, actions) -> {
            switch (actions) {
                case MORE:
                    showMenu(position, smsTemplate);
                    break;
                case OPEN:
                    openTemplate(smsTemplate);
                    break;
            }
        });
        binding.templatesList.setAdapter(adapter);
        refreshView();
    }

    private void showMenu(int position, SmsTemplate smsTemplate) {
        String[] items = new String[]{getString(R.string.edit), getString(R.string.delete)};
        SuperUtil.showLCAM(getContext(), item -> {
            switch (item) {
                case 0:
                    openTemplate(smsTemplate);
                    break;
                case 1:
                    deleteTemplate(smsTemplate);
                    break;
            }
        }, items);
    }

    private void openTemplate(SmsTemplate smsTemplate) {
        startActivity(new Intent(getContext(), TemplateActivity.class)
                .putExtra(Constants.INTENT_ID, smsTemplate.getKey()));
    }

    private void deleteTemplate(SmsTemplate smsTemplate) {
        viewModel.deleteSmsTemplate(smsTemplate);
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
    }

    private void showTemplates(List<SmsTemplate> smsTemplates) {
        filterController.setOriginal(smsTemplates);
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
    public void onChanged(@NonNull List<SmsTemplate> result) {
        adapter.setData(result);
        binding.templatesList.smoothScrollToPosition(0);
        refreshView();
    }
}
