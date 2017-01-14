package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FilterCallback;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.databinding.FragmentNotesBinding;
import com.elementary.tasks.notes.ActivityCreateNote;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.NotePreviewActivity;
import com.elementary.tasks.notes.NotesRecyclerAdapter;
import com.elementary.tasks.notes.SyncNotes;

import java.io.File;
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

public class NotesFragment extends BaseNavigationFragment {
    public static final int MENU_ITEM_DELETE = 12;

    private FragmentNotesBinding binding;
    private NotesRecyclerAdapter mAdapter;
    private boolean enableGrid = false;
    private List<NoteItem> mDataList = new ArrayList<>();

    private SimpleListener mEventListener = new SimpleListener() {
        @Override
        public void onItemClicked(int position, View view) {
            String id = mAdapter.getItem(position).getKey();
            previewNote(id, view);
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            String showIn = getString(R.string.show_in_status_bar);
            showIn = showIn.substring(0, showIn.length() - 1);
            final String[] items = {getString(R.string.open), getString(R.string.share),
                    showIn, getString(R.string.change_color), getString(R.string.edit),
                    getString(R.string.delete)};
            Dialogues.showLCAM(mContext, item -> {
                NoteItem noteItem = mAdapter.getItem(position);
                switch (item) {
                    case 0:
                        previewNote(noteItem.getKey(), view);
                        break;
                    case 1:
                        shareNote(noteItem);
                        break;
                    case 2:
                        showInStatusBar(noteItem.getKey());
                        break;
                    case 3:
                        selectColor(position, noteItem.getKey());
                        break;
                    case 4:
                        mContext.startActivity(new Intent(mContext, ActivityCreateNote.class)
                                .putExtra(Constants.INTENT_ID, noteItem.getKey()));
                        break;
                    case 5:
                        RealmDb.getInstance().deleteNote(noteItem);
                        mAdapter.remove(position);
                        refreshView();
                        break;
                }
            }, items);
        }
    };
    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            binding.recyclerView.scrollToPosition(0);
            refreshView();
        }
    };
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

    private SearchView.OnCloseListener mCloseListener = () -> {
        showData();
        return true;
    };
    private SyncNotes.SyncListener mSyncListener = b -> showData();

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
        inflater.inflate(R.menu.notes_menu, menu);
        MenuItem item = menu.findItem(R.id.action_list);
        if (item != null) {
            item.setIcon(!enableGrid ? R.drawable.ic_view_quilt_white_24dp : R.drawable.ic_view_list_white_24dp);
            item.setTitle(!enableGrid ? mContext.getString(R.string.grid_view) : mContext.getString(R.string.list_view));
        }
        if (RealmDb.getInstance().getAllNotes(null).size() != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all));
        }
        mSearchMenu = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (mSearchMenu != null) {
            mSearchView = (SearchView) mSearchMenu.getActionView();
        }
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setOnQueryTextListener(queryTextListener);
            mSearchView.setOnCloseListener(mCloseListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void shareNote(NoteItem noteItem){
        File file = BackupTool.getInstance().createNote(noteItem);
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(mContext, getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyUtil.sendNote(file, mContext, noteItem.getSummary());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new SyncNotes(mContext, mSyncListener).execute();
                break;
            case R.id.action_order:
                showDialog();
                break;
            case MENU_ITEM_DELETE:
                deleteDialog();
                break;
            case R.id.action_list:
                enableGrid = !enableGrid;
                Prefs.getInstance(mContext).setNotesGridEnabled(enableGrid);
                showData();
                getActivity().invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        initList();
        return binding.getRoot();
    }

    private void initList() {
        refreshView();
    }

    private void showDialog() {
        final CharSequence[] items = {mContext.getString(R.string.by_date_az),
                mContext.getString(R.string.by_date_za),
                mContext.getString(R.string.name_az),
                mContext.getString(R.string.name_za)};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.order));
        builder.setItems(items, (dialog, which) -> {
            String value = null;
            if (which == 0) {
                value = Constants.ORDER_DATE_A_Z;
            } else if (which == 1) {
                value = Constants.ORDER_DATE_Z_A;
            } else if (which == 2) {
                value = Constants.ORDER_NAME_A_Z;
            } else if (which == 3) {
                value = Constants.ORDER_NAME_Z_A;
            }
            Prefs.getInstance(mContext).setNoteOrder(value);
            dialog.dismiss();
            showData();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.notes));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(view -> startActivity(new Intent(mContext, ActivityCreateNote.class)));
        }
        showData();
    }

    private void showData() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        enableGrid = Prefs.getInstance(mContext).isNotesGridEnabled();
        if (enableGrid) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        binding.recyclerView.setLayoutManager(layoutManager);
        mDataList = RealmDb.getInstance().getAllNotes(Prefs.getInstance(mContext).getNoteOrder());
        mAdapter = new NotesRecyclerAdapter(getActivity(), mDataList, mFilterCallback);
        mAdapter.setEventListener(mEventListener);
        binding.recyclerView.setAdapter(mAdapter);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshView();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setMessage(R.string.delete_all_notes);
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteAll();
            showData();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAll() {
        List<NoteItem> list = RealmDb.getInstance().getAllNotes(null);
        for (NoteItem item : list) {
            RealmDb.getInstance().deleteNote(item);
        }
    }

    private void previewNote(String id, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(mContext, NotePreviewActivity.class);
            intent.putExtra(Constants.INTENT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, transitionName);
            mContext.startActivity(intent, options.toBundle());
        } else {
            mContext.startActivity(new Intent(mContext, NotePreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        }
    }

    private void showInStatusBar(String id) {
        NoteItem item = RealmDb.getInstance().getNote(id);
        if (item != null) {
            new Notifier(mContext).showNoteNotification(item);
        }
    }

    private void selectColor(int position, final String id) {
        String[] items = {getString(R.string.red), getString(R.string.purple),
                getString(R.string.green), getString(R.string.green_light),
                getString(R.string.blue), getString(R.string.blue_light),
                getString(R.string.yellow), getString(R.string.orange),
                getString(R.string.cyan), getString(R.string.pink),
                getString(R.string.teal), getString(R.string.amber)};
        if (Module.isPro()) {
            items = new String[]{getString(R.string.red), getString(R.string.purple),
                    getString(R.string.green), getString(R.string.green_light),
                    getString(R.string.blue), getString(R.string.blue_light),
                    getString(R.string.yellow), getString(R.string.orange),
                    getString(R.string.cyan), getString(R.string.pink),
                    getString(R.string.teal), getString(R.string.amber),
                    getString(R.string.dark_purple), getString(R.string.dark_orange),
                    getString(R.string.lime), getString(R.string.indigo)};
        }
        Dialogues.showLCAM(mContext, item -> {
            RealmDb.getInstance().changeNoteColor(id, item);
            if (mAdapter != null) mAdapter.notifyChanged(position, id);
        }, items);
    }

    private void refreshView() {
        if (mAdapter == null || mAdapter.getItemCount() == 0) {
            binding.emptyItem.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyItem.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
