package com.elementary.tasks.navigation.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.elementary.tasks.core.adapter.FilterableAdapter;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.databinding.FragmentNotesBinding;
import com.elementary.tasks.notes.CreateNoteActivity;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.notes.NotePreviewActivity;
import com.elementary.tasks.notes.NotesRecyclerAdapter;
import com.elementary.tasks.notes.SyncNotes;

import java.io.File;
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
    private ProgressDialog mProgress;

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
            Dialogues.showLCAM(getContext(), item -> {
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
                        getContext().startActivity(new Intent(getContext(), CreateNoteActivity.class)
                                .putExtra(Constants.INTENT_ID, noteItem.getKey()));
                        break;
                    case 5:
                        RealmDb.getInstance().deleteNote(noteItem);
                        mAdapter.removeItem(position);
                        refreshView();
                        break;
                }
            }, items);
        }
    };
    private SearchView mSearchView = null;
    private MenuItem mSearchMenu = null;

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (mAdapter != null) mAdapter.filter(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mAdapter != null) mAdapter.filter(newText);
            return false;
        }
    };

    private SearchView.OnCloseListener mCloseListener = () -> {
        showData();
        return true;
    };
    private SyncNotes.SyncListener mSyncListener = b -> showData();
    private FilterableAdapter.Filter<NoteItem, String> mFilterCallback = new FilterableAdapter.Filter<NoteItem, String>() {
        @Override
        public boolean filter(NoteItem noteItem, String query) {
            return noteItem.getSummary().toLowerCase().contains(query.toLowerCase());
        }

        @Override
        public void onFilterEnd(List<NoteItem> list, int size, String query) {
            binding.recyclerView.smoothScrollToPosition(0);
            refreshView();
        }
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
        inflater.inflate(R.menu.notes_menu, menu);
        MenuItem item = menu.findItem(R.id.action_list);
        if (item != null) {
            item.setIcon(!enableGrid ? R.drawable.ic_view_quilt_white_24dp : R.drawable.ic_view_list_white_24dp);
            item.setTitle(!enableGrid ? getString(R.string.grid_view) : getString(R.string.list_view));
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
        showProgress();
        BackupTool.getInstance().createNote(noteItem, file -> sendNote(noteItem, file));
    }

    private void sendNote(NoteItem noteItem, File file) {
        hideProgress();
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(getContext(), getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyUtil.sendNote(file, getContext(), noteItem.getSummary());
    }

    private void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    private void showProgress() {
        mProgress = ProgressDialog.show(getContext(), null, getString(R.string.please_wait), true, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                new SyncNotes(getContext(), mSyncListener).execute();
                break;
            case R.id.action_order:
                showDialog();
                break;
            case MENU_ITEM_DELETE:
                deleteDialog();
                break;
            case R.id.action_list:
                enableGrid = !enableGrid;
                getPrefs().setNotesGridEnabled(enableGrid);
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
        final CharSequence[] items = {getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.name_az),
                getString(R.string.name_za)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
            getPrefs().setNoteOrder(value);
            dialog.dismiss();
            showData();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.notes));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> startActivity(new Intent(getContext(), CreateNoteActivity.class)));
            getCallback().onScrollChanged(binding.recyclerView);
        }
        showData();
    }

    private void showData() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        enableGrid = getPrefs().isNotesGridEnabled();
        if (enableGrid) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        binding.recyclerView.setLayoutManager(layoutManager);
        mAdapter = new NotesRecyclerAdapter(RealmDb.getInstance().getAllNotes(getPrefs().getNoteOrder()), mFilterCallback);
        mAdapter.setEventListener(mEventListener);
        binding.recyclerView.setAdapter(mAdapter);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshView();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
            Intent intent = new Intent(getContext(), NotePreviewActivity.class);
            intent.putExtra(Constants.INTENT_ID, id);
            String transitionName = "image";
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, transitionName);
            getContext().startActivity(intent, options.toBundle());
        } else {
            getContext().startActivity(new Intent(getContext(), NotePreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, id));
        }
    }

    private void showInStatusBar(String id) {
        NoteItem item = RealmDb.getInstance().getNote(id);
        if (item != null) {
            new Notifier(getContext()).showNoteNotification(item);
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
        Dialogues.showLCAM(getContext(), item -> {
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
