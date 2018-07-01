package com.elementary.tasks.reminder.lists;

import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.async.SyncTask;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.view_models.reminders.ActiveRemindersViewModel;
import com.elementary.tasks.core.views.FilterView;
import com.elementary.tasks.databinding.FragmentRemindersBinding;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;
import com.elementary.tasks.reminder.ReminderUpdateEvent;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;
import com.elementary.tasks.reminder.lists.filters.ReminderFilterController;
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity;
import com.elementary.tasks.reminder.preview.ShoppingPreviewActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
public class RemindersFragment extends BaseNavigationFragment implements SyncTask.SyncListener,
        FilterCallback<Reminder> {

    private FragmentRemindersBinding binding;
    private ActiveRemindersViewModel viewModel;

    private RemindersRecyclerAdapter mAdapter = new RemindersRecyclerAdapter();

    @NonNull
    private ArrayList<String> mGroupsIds = new ArrayList<>();
    private List<FilterView.Filter> filters = new ArrayList<>();
    @NonNull
    private ReminderFilterController filterController = new ReminderFilterController(this);

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) filterController.setSearchValue(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) filterController.setSearchValue(newText);
            if (!getCallback().isFiltersVisible()) {
                showRemindersFilter();
            }
            return false;
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> {
        refreshFilters();
        return false;
    };
    private RecyclerListener mEventListener = new RecyclerListener() {
        @Override
        public void onItemSwitched(int position, View view) {
            switchReminder(position);
        }

        @Override
        public void onItemClicked(int position, View view) {
            if (view.getId() == R.id.button_more) {
                showActionDialog(position, view);
            } else {
                Reminder reminder = mAdapter.getItem(position);
                if (reminder != null) {
                    previewReminder(reminder.getUniqueId(), reminder.getType());
                }
            }
        }

        @Override
        public void onItemLongClicked(int position, View view) {
        }
    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_active_menu, menu);
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
            mSearchView.setOnCloseListener(mSearchCloseListener);
            mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    if (!getCallback().isFiltersVisible()) {
                        showRemindersFilter();
                    }
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new SyncTask(getContext(), this, false).execute();
                break;
            case R.id.action_voice:
                if (getCallback() != null) {
                    getCallback().onVoiceAction();
                }
                break;
            case R.id.action_filter:
                if (getCallback().isFiltersVisible()) {
                    getCallback().hideFilters();
                } else {
                    showRemindersFilter();
                }
                break;
            case R.id.action_exit:
                getActivity().finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRemindersBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ActiveRemindersViewModel.class);
        viewModel.events.observe(this, reminders -> {
            if (reminders != null) {
                showData(reminders);
            }
        });
    }

    private void showData(List<Reminder> result) {
        filterController.setOriginal(result);
        reloadView();
        refreshFilters();
    }

    private void refreshFilters() {
        filters.clear();
        addDateFilter(filters);
        if (viewModel.allGroups.getValue() != null) {
            addGroupFilter(viewModel.allGroups.getValue());
        }
        addTypeFilter(filters);
        addStatusFilter(filters);
        if (getCallback().isFiltersVisible()) {
            showRemindersFilter();
        }
    }

    private void showActionDialog(int position, View view) {
        Reminder item1 = mAdapter.getItem(position);
        if (item1 == null) return;
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_trash)};
        Dialogues.showPopup(getContext(), view, item -> {
            switch (item) {
                case 0:
                    previewReminder(item1.getUniqueId(), item1.getType());
                    break;
                case 1:
                    editReminder(item1.getUniqueId());
                    break;
                case 2:
                    changeGroup(item1);
                    break;
                case 3:
                    viewModel.moveToTrash(item1);
                    break;
            }
        }, items);
    }

    private void editReminder(int uuId) {
        startActivity(new Intent(getContext(), CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void switchReminder(int position) {
        Reminder reminder = mAdapter.getItem(position);
        if (reminder == null) return;
        viewModel.toggleReminder(reminder);
    }

    private void initList() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter.setEventListener(mEventListener);
        binding.recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.tasks));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> startActivity(new Intent(getContext(), CreateReminderActivity.class)));
            getCallback().onScrollChanged(binding.recyclerView);
        }
    }

    @Subscribe
    public void onEvent(ReminderUpdateEvent e) {

    }

    private void previewReminder(final int id, final int type) {
        if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            getContext().startActivity(new Intent(getContext(), ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        } else {
            getContext().startActivity(new Intent(getContext(), ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        }
    }

    private void reloadView() {
        if (mAdapter.getItemCount() > 0) {
            if (binding.recyclerView.getVisibility() == View.GONE)
                binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void showRemindersFilter() {
        getCallback().addFilters(filters, true);
    }

    private void addStatusFilter(List<FilterView.Filter> filters) {
        List<Reminder> reminders = filterController.getOriginal();
        if (reminders.size() == 0) {
            return;
        }
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                filterController.setStatusValue(id);
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {

            }
        });
        filter.add(getFilterAllElement());
        filter.add(new FilterView.FilterElement(R.drawable.ic_power_button, getString(R.string.enabled4), 1));
        filter.add(new FilterView.FilterElement(R.drawable.ic_off, getString(R.string.disabled), 2));
        filters.add(filter);
    }

    @NonNull
    private FilterView.FilterElement getFilterAllElement() {
        return new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true);
    }

    private void addDateFilter(List<FilterView.Filter> filters) {
        List<Reminder> reminders = filterController.getOriginal();
        if (reminders.size() == 0) {
            return;
        }
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                filterController.setRangeValue(id);
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {

            }
        });
        filter.add(getFilterAllElement());
        filter.add(new FilterView.FilterElement(R.drawable.ic_push_pin, getString(R.string.permanent), 1));
        filter.add(new FilterView.FilterElement(R.drawable.ic_calendar_illustration, getString(R.string.today), 2));
        filter.add(new FilterView.FilterElement(R.drawable.ic_calendar_illustration, getString(R.string.tomorrow), 3));
        filters.add(filter);
    }

    private void addTypeFilter(List<FilterView.Filter> filters) {
        List<Reminder> reminders = filterController.getOriginal();
        if (reminders.size() == 0) {
            return;
        }
        Set<Integer> types = new LinkedHashSet<>();
        for (Reminder reminder : reminders) {
            types.add(reminder.getType());
        }
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                filterController.setTypeValue(id);
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {

            }
        });
        filter.add(getFilterAllElement());
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (Integer integer : types) {
            filter.add(new FilterView.FilterElement(util.getReminderIllustration(integer), ReminderUtils.getType(getContext(), integer), integer));
        }
        if (filter.size() != 0) {
            filters.add(filter);
        }
    }

    private void addGroupFilter(List<Group> groups) {
        mGroupsIds = new ArrayList<>();
        FilterView.Filter filter = new FilterView.Filter(new FilterView.FilterElementClick() {
            @Override
            public void onClick(View view, int id) {
                if (id == 0) {
                    filterController.setGroupValue(null);
                } else {
                    filterController.setGroupValue(mGroupsIds.get(id - 1));
                }
            }

            @Override
            public void onMultipleSelected(View view, List<Integer> ids) {
                List<String> groups = new ArrayList<>();
                for (Integer i : ids) groups.add(mGroupsIds.get(i - 1));
                filterController.setGroupValues(groups);
            }
        });
        filter.add(new FilterView.FilterElement(R.drawable.ic_bell_illustration, getString(R.string.all), 0, true));
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (int i = 0; i < groups.size(); i++) {
            Group item = groups.get(i);
            filter.add(new FilterView.FilterElement(util.getCategoryIndicator(item.getColor()), item.getTitle(), i + 1));
            mGroupsIds.add(item.getUuId());
        }
        filters.add(filter);
    }

    private void changeGroup(Reminder reminder) {
        mGroupsIds.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.select_dialog_item);
        List<Group> groups = viewModel.allGroups.getValue();
        for (Group item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            dialog.dismiss();
            String catId = mGroupsIds.get(which);
            if (reminder.getGroupUuId().matches(catId)) {
                Toast.makeText(getContext(), getString(R.string.same_group), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.changeGroup(reminder, catId);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void endExecution(boolean b) {
    }

    @Override
    public void onChanged(@NonNull List<Reminder> result) {
        mAdapter.setData(result);
        binding.recyclerView.smoothScrollToPosition(0);
        reloadView();
    }
}