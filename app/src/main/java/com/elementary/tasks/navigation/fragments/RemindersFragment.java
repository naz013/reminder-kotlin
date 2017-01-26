package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.file_explorer.FilterCallback;
import com.elementary.tasks.core.interfaces.RealmCallback;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.DataLoader;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentRemindersBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.ReminderPreviewActivity;
import com.elementary.tasks.reminder.RemindersRecyclerAdapter;
import com.elementary.tasks.reminder.ShoppingPreviewActivity;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
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

public class RemindersFragment extends BaseNavigationFragment implements SyncTask.SyncListener {

    private FragmentRemindersBinding binding;
    private RecyclerView mRecyclerView;

    private RemindersRecyclerAdapter mAdapter;

    private List<Reminder> mDataList = new ArrayList<>();
    private ArrayList<String> mGroupsIds;
    private String mLastGroupId;

    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) mAdapter.filter(query, mDataList);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) mAdapter.filter(newText, mDataList);
            return false;
        }
    };
    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            mRecyclerView.scrollToPosition(0);
            reloadView();
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> false;
    private RecyclerListener mEventListener = new RecyclerListener() {
        @Override
        public void onItemSwitched(int position, View view) {
            switchReminder(position);
        }

        @Override
        public void onItemClicked(int position, View view) {
            Reminder reminder = mDataList.get(position);
            previewReminder(view, reminder.getUuId(), reminder.getType());
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            showActionDialog(position, view);
        }
    };
    private RealmCallback<List<Reminder>> mLoadCallback = this::showData;

    private void showData(List<Reminder> result) {
        mDataList = result;
        mAdapter = new RemindersRecyclerAdapter(mContext, mDataList, mFilterCallback);
        mAdapter.setEventListener(mEventListener);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        reloadView();
        getActivity().invalidateOptionsMenu();
    }

    private void showActionDialog(int position, View view) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.change_group), getString(R.string.move_to_trash)};
        Dialogues.showLCAM(mContext, item -> {
            Reminder item1 = mAdapter.getItem(position);
            switch (item){
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
                    if (RealmDb.getInstance().moveToTrash(item1.getUuId())) {
                        EventControl control = EventControlImpl.getController(mContext, item1.setRemoved(true));
                        control.stop();
                        mAdapter.removeItem(position);
                        reloadView();
                    }
                    break;
            }
        }, items);
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(mContext, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void switchReminder(int position) {
        EventControl control = EventControlImpl.getController(mContext, mAdapter.getItem(position));
        if (!control.onOff()) {
            Toast.makeText(mContext, R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show();
        }
        loadData(mLastGroupId);
    }

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
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setOnQueryTextListener(queryTextListener);
            mSearchView.setOnCloseListener(mSearchCloseListener);
        }
        if (mDataList.size() == 0){
            menu.findItem(R.id.action_filter).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        } else {
            menu.findItem(R.id.action_filter).setVisible(true);
            menu.findItem(R.id.action_search).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                new SyncTask(mContext, this, false).execute();
                break;
            case R.id.action_voice:
                if (mCallback != null) {
                    mCallback.onVoiceAction();
                }
                break;
            case R.id.action_filter:
                filterDialog();
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

    private void initList() {
        mRecyclerView = binding.recyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.tasks));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(view -> startActivity(new Intent(mContext, CreateReminderActivity.class)));
        }
        loadData(mLastGroupId);
    }

    private void previewReminder(final View view, final String id, final int type){
        if (Reminder.isSame(type, Reminder.BY_DATE_SHOP)){
            mContext.startActivity(new Intent(mContext, ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        } else {
            mContext.startActivity(new Intent(mContext, ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        }
    }

    private void loadData(String groupId) {
        mLastGroupId = groupId;
        if (groupId != null) {
            DataLoader.loadActiveReminder(groupId, mLoadCallback);
        } else {
            DataLoader.loadActiveReminder(mLoadCallback);
        }
    }

    private void reloadView() {
        int size = mAdapter.getItemCount();
        if (size > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void filterDialog(){
        mGroupsIds = new ArrayList<>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext, android.R.layout.select_dialog_item);
        arrayAdapter.add(getString(R.string.all));
        List<GroupItem> groups = RealmDb.getInstance().getAllGroups();
        for (GroupItem item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            if (which == 0) {
                loadData(null);
            } else {
                String catId = mGroupsIds.get(which - 1);
                loadData(catId);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void changeGroup(final String oldUuId, final String id){
        mGroupsIds = new ArrayList<>();
        mGroupsIds.clear();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                mContext, android.R.layout.select_dialog_item);
        List<GroupItem> groups = RealmDb.getInstance().getAllGroups();
        for (GroupItem item : groups) {
            arrayAdapter.add(item.getTitle());
            mGroupsIds.add(item.getUuId());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_group));
        builder.setAdapter(arrayAdapter, (dialog, which) -> {
            dialog.dismiss();
            String catId = mGroupsIds.get(which);
            if (oldUuId.matches(catId)) {
                Toast.makeText(mContext, getString(R.string.same_group), Toast.LENGTH_SHORT).show();
                return;
            }
            RealmDb.getInstance().changeReminderGroup(id, catId);
            loadData(mLastGroupId);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void endExecution(boolean b) {
        if (b) loadData(mLastGroupId);
    }
}
