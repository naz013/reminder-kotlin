package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
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
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.interfaces.RealmCallback;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.DataLoader;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ReminderUtils;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.FilterView;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentRemindersBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.ReminderPreviewActivity;
import com.elementary.tasks.reminder.ReminderUpdateEvent;
import com.elementary.tasks.reminder.RemindersRecyclerAdapter;
import com.elementary.tasks.reminder.ShoppingPreviewActivity;
import com.elementary.tasks.reminder.UpdateFilesAsync;
import com.elementary.tasks.reminder.filters.FilterCallback;
import com.elementary.tasks.reminder.filters.ReminderFilterController;
import com.elementary.tasks.reminder.models.Reminder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private static final String TAG = "RemindersFragment";

    private FragmentRemindersBinding binding;
    private RecyclerView mRecyclerView;

    private RemindersRecyclerAdapter mAdapter;

    @NonNull
    private ArrayList<String> mGroupsIds = new ArrayList<>();
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
                previewReminder(view, reminder.getUuId(), reminder.getType());
            }
        }

        @Override
        public void onItemLongClicked(int position, View view) {
        }
    };
    private RealmCallback<List<Reminder>> mLoadCallback = this::showData;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRemindersBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    private void showData(List<Reminder> result) {
        filterController.setOriginal(result);
        reloadView();
    }

    private void refreshFilters() {
        if (getCallback().isFiltersVisible()) {
            showRemindersFilter();
        }
    }

    private void showActionDialog(int position, View view) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_trash)};
        Dialogues.showPopup(getContext(), view, item -> {
            Reminder item1 = mAdapter.getItem(position);
            switch (item) {
                case 0:
                    previewReminder(view, item1.getUuId(), item1.getType());
                    break;
                case 1:
                    editReminder(item1.getUuId());
                    break;
                case 2:
                    changeGroup(item1.getGroupUuId(), item1.getUuId());
                    break;
                case 3:
                    moveToTrash(item1, position);
                    break;
            }
        }, items);
    }

    private void moveToTrash(Reminder item1, int position) {
        EventControl control = EventControlFactory.getController(getContext(), item1.setRemoved(true));
        control.stop();
        mAdapter.removeItem(position);
        refreshFilters();
        reloadView();
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(getContext(), CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void switchReminder(int position) {
        Reminder reminder = mAdapter.getItem(position);
        EventControl control = EventControlFactory.getController(getContext(), reminder);
        if (!control.onOff()) {
            Toast.makeText(getContext(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
        }
        new UpdateFilesAsync(getContext()).execute(reminder);
        loadData();
    }

    private void initList() {
        mRecyclerView = binding.recyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new RemindersRecyclerAdapter(getContext());
        mAdapter.setEventListener(mEventListener);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
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
        loadData();
    }

    @Subscribe
    public void onEvent(ReminderUpdateEvent e) {
        loadData();
    }

    private void previewReminder(final View view, final String id, final int type) {
        if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)) {
            getContext().startActivity(new Intent(getContext(), ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        } else {
            getContext().startActivity(new Intent(getContext(), ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        }
    }

    private void loadData() {
        DataLoader.loadActiveReminder(mLoadCallback);
    }

    private void reloadView() {
        if (mAdapter.getItemCount() > 0) {
            if (mRecyclerView.getVisibility() == View.GONE)
                mRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void showRemindersFilter() {
        List<FilterView.Filter> filters = new ArrayList<>();
        addDateFilter(filters);
        addGroupFilter(filters);
        addTypeFilter(filters);
        addStatusFilter(filters);
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
        filter.add(new FilterView.FilterElement(0, getString(R.string.permanent), 1));
        filter.add(new FilterView.FilterElement(0, getString(R.string.today), 2));
        filter.add(new FilterView.FilterElement(0, getString(R.string.tomorrow), 3));
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

    private void addGroupFilter(List<FilterView.Filter> filters) {
        mGroupsIds.clear();
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
                Log.d(TAG, "onMultipleSelected: " + ids);
                List<String> groups = new ArrayList<>();
                for (Integer i : ids) {
                    if (i == 0) {
                        filterController.setGroupValue(null);
                        break;
                    } else {
                        groups.add(mGroupsIds.get(i - 1));
                    }
                }
                filterController.setGroupValues(groups);
            }
        });
        filter.setChoiceMode(FilterView.ChoiceMode.MULTI);
        filter.add(getFilterAllElement());
        List<GroupItem> groups = RealmDb.getInstance().getAllGroups();
        ThemeUtil util = ThemeUtil.getInstance(getContext());
        for (int i = 0; i < groups.size(); i++) {
            GroupItem item = groups.get(i);
            filter.add(new FilterView.FilterElement(util.getCategoryIndicator(item.getColor()), item.getTitle(), i + 1));
            mGroupsIds.add(item.getUuId());
        }
        filters.add(filter);
    }

    private void changeGroup(final String oldUuId, final String id) {
        mGroupsIds.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.select_dialog_item);
        List<GroupItem> groups = RealmDb.getInstance().getAllGroups();
        for (GroupItem item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            dialog.dismiss();
            String catId = mGroupsIds.get(which);
            if (oldUuId.matches(catId)) {
                Toast.makeText(getContext(), getString(R.string.same_group), Toast.LENGTH_SHORT).show();
                return;
            }
            RealmDb.getInstance().changeReminderGroup(id, catId);
            loadData();
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
        if (b) loadData();
    }

    @Override
    public void onChanged(@NonNull List<Reminder> result) {
        mAdapter.setData(result);
        mRecyclerView.smoothScrollToPosition(0);
        reloadView();
    }
}