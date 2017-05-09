package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.FragmentGoogleTasksBinding;
import com.elementary.tasks.google_tasks.SyncGoogleTasksAsync;
import com.elementary.tasks.google_tasks.TaskActivity;
import com.elementary.tasks.google_tasks.TaskItem;
import com.elementary.tasks.google_tasks.TaskListActivity;
import com.elementary.tasks.google_tasks.TaskListAsync;
import com.elementary.tasks.google_tasks.TaskListItem;
import com.elementary.tasks.google_tasks.TaskListWrapperItem;
import com.elementary.tasks.google_tasks.TaskPagerAdapter;
import com.elementary.tasks.google_tasks.TasksCallback;
import com.elementary.tasks.google_tasks.TasksConstants;
import com.elementary.tasks.google_tasks.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
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

public class GoogleTasksFragment extends BaseNavigationFragment {

    public static final int MENU_ITEM_EDIT = 12;
    public static final int MENU_ITEM_DELETE = 13;
    public static final int MENU_ITEM_CLEAR = 14;

    private static final String TAG = "TasksFragment";
    private ViewPager pager;
    private ArrayList<TaskListWrapperItem> taskListDatum;
    private int currentPos;
    private ProgressDialog mDialog;

    private TasksCallback mTasksCallback = new TasksCallback() {
        @Override
        public void onFailed() {
            Toast.makeText(getContext(), R.string.failed_to_sync_google_tasks, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onComplete() {
            loadData();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_menu, menu);
        if (currentPos != 0) {
            menu.add(Menu.NONE, MENU_ITEM_EDIT, 100, R.string.edit_list);
            String listId = taskListDatum.get(currentPos).getTaskList().getListId();
            TaskListItem listItem = RealmDb.getInstance().getTaskList(listId);
            if (listItem != null) {
                if (listItem.getDef() != 1) {
                    menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_list));
                }
            }
            menu.add(Menu.NONE, MENU_ITEM_CLEAR, 100, R.string.delete_completed_tasks);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new SyncGoogleTasksAsync(getContext(), mTasksCallback).execute();
                return true;
            case R.id.action_add_list:
                startActivity(new Intent(getContext(), TaskListActivity.class));
                return true;
            case MENU_ITEM_EDIT:
                if (currentPos != 0) {
                    startActivity(new Intent(getContext(), TaskListActivity.class)
                            .putExtra(Constants.INTENT_ID, taskListDatum.get(currentPos).getTaskList().getListId()));
                }
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case MENU_ITEM_CLEAR:
                clearList();
                return true;
            case R.id.action_order:
                showDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentGoogleTasksBinding binding = FragmentGoogleTasksBinding.inflate(inflater, container, false);
        pager = binding.pager;
        return binding.getRoot();
    }

    private void showProgressDialog(String title) {
        mDialog = ProgressDialog.show(getContext(), null, title, true, false);
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            EventBus.getDefault().register(this);
        } catch (org.greenrobot.eventbus.EventBusException ignored) {}
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.google_tasks));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> addNewTask());
        }
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            EventBus.getDefault().unregister(this);
        } catch (org.greenrobot.eventbus.EventBusException ignored) {}
    }

    @Subscribe
    public void onEvent(UpdateEvent event) {
        loadData();
    }

    private void addNewTask() {
        getContext().startActivity(new Intent(getContext(), TaskActivity.class)
                .putExtra(Constants.INTENT_ID, taskListDatum.get(currentPos).getTaskList().getListId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.CREATE));
    }

    private void showDialog() {
        final String[] items = {getString(R.string.default_string),
                getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.active_first),
                getString(R.string.completed_first)};
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                getPrefs().setTasksOrder(Constants.ORDER_DEFAULT);
            } else if (which == 1) {
                getPrefs().setTasksOrder(Constants.ORDER_DATE_A_Z);
            } else if (which == 2) {
                getPrefs().setTasksOrder(Constants.ORDER_DATE_Z_A);
            } else if (which == 3) {
                getPrefs().setTasksOrder(Constants.ORDER_COMPLETED_Z_A);
            } else if (which == 4) {
                getPrefs().setTasksOrder(Constants.ORDER_COMPLETED_A_Z);
            }
            dialog.dismiss();
            loadData();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setMessage(getString(R.string.delete_this_list));
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            deleteList();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteList() {
        TaskListItem taskListItem = taskListDatum.get(currentPos).getTaskList();
        if (taskListItem != null) {
            String listId = taskListItem.getListId();
            showProgressDialog(getString(R.string.deleting_list));
            new TaskListAsync(getContext(), null, 0, listId, TasksConstants.DELETE_TASK_LIST, new TasksCallback() {
                @Override
                public void onFailed() {
                    hideDialog();
                }

                @Override
                public void onComplete() {
                    RealmDb.getInstance().deleteTaskList(taskListItem.getListId());
                    RealmDb.getInstance().deleteTasks(listId);
                    int def = taskListItem.getDef();
                    if (def == 1) {
                        TaskListItem listItem = RealmDb.getInstance().getTaskLists().get(0);
                        RealmDb.getInstance().setDefault(listItem.getListId());
                    }
                    getPrefs().setLastGoogleList(0);
                    hideDialog();
                    loadData();
                }
            }).execute();
        }
    }

    private void loadData() {
        taskListDatum = new ArrayList<>();
        List<TaskListItem> taskLists = getTaskLists();
        if (taskLists.size() == 0) return;
        Map<String, Integer> colors = new HashMap<>();
        for (int i = 0; i < taskLists.size(); i++) {
            TaskListItem item = taskLists.get(i);
            taskListDatum.add(new TaskListWrapperItem(item, getList(item.getListId()), i));
            if (i > 0) colors.put(item.getListId(), item.getColor());
        }
        int pos = getPrefs().getLastGoogleList();
        TaskPagerAdapter pagerAdapter;
        if (Module.isJellyMR1()) {
            pagerAdapter = new TaskPagerAdapter(getChildFragmentManager(), taskListDatum, colors);
        } else {
            pagerAdapter = new TaskPagerAdapter(getFragmentManager(), taskListDatum, colors);
        }
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                updateScreen(i);
                getPrefs().setLastGoogleList(i);
                currentPos = i;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        pager.setCurrentItem(pos < taskListDatum.size() ? pos : 0);
        updateScreen(pos < taskListDatum.size() ? pos : 0);
    }

    private void updateScreen(int pos) {
        if (getCallback() != null) {
            ThemeUtil mColor = ThemeUtil.getInstance(getContext());
            if (pos == 0) {
                getCallback().onTitleChange(getString(R.string.all));
                getCallback().onThemeChange(mColor.getColor(mColor.colorPrimary()),
                        mColor.getColor(mColor.colorPrimaryDark()),
                        mColor.getColor(mColor.colorAccent()));
            } else {
                TaskListItem taskList = taskListDatum.get(pos).getTaskList();
                getCallback().onTitleChange(taskList.getTitle());
                int tmp = taskList.getColor();
                getCallback().onThemeChange(mColor.getColor(mColor.colorPrimary(tmp)),
                        mColor.getColor(mColor.colorPrimaryDark(tmp)),
                        mColor.getColor(mColor.colorAccent(tmp)));
            }
        }
    }

    private List<TaskListItem> getTaskLists() {
        ArrayList<TaskListItem> lists = new ArrayList<>();
        TaskListItem zeroItem = new TaskListItem();
        zeroItem.setTitle(getString(R.string.all));
        zeroItem.setColor(25);
        lists.add(zeroItem);
        lists.addAll(RealmDb.getInstance().getTaskLists());
        return lists;
    }

    private List<TaskItem> getList(String listId) {
        List<TaskItem> mData = new ArrayList<>();
        String orderPrefs = getPrefs().getTasksOrder();
        if (listId == null) {
            List<TaskItem> list = RealmDb.getInstance().getTasks(orderPrefs);
            mData.addAll(list);
        } else {
            List<TaskItem> list = RealmDb.getInstance().getTasks(listId, orderPrefs);
            mData.addAll(list);
        }
        return mData;
    }

    private void clearList() {
        String listId = taskListDatum.get(currentPos).getTaskList().getListId();
        RealmDb.getInstance().deleteCompletedTasks(listId);
        new TaskListAsync(getContext(), null, 0, listId, TasksConstants.CLEAR_TASK_LIST, mTasksCallback).execute();
    }
}
