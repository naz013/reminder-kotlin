package com.elementary.tasks.notes.list;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.view_models.notes.NotesViewModel;
import com.elementary.tasks.databinding.FragmentNotesBinding;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;
import com.elementary.tasks.notes.create.CreateNoteActivity;
import com.elementary.tasks.notes.preview.NotePreviewActivity;
import com.elementary.tasks.notes.work.SyncNotes;
import com.elementary.tasks.reminder.lists.filters.FilterCallback;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
public class NotesFragment extends BaseNavigationFragment implements FilterCallback<Note> {

    public static final int MENU_ITEM_DELETE = 12;

    private FragmentNotesBinding binding;
    private NotesViewModel viewModel;

    private NotesRecyclerAdapter mAdapter = new NotesRecyclerAdapter();
    private boolean enableGrid = false;
    private ProgressDialog mProgress;

    @NonNull
    private NoteFilterController filterController = new NoteFilterController(this);

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
        inflater.inflate(R.menu.notes_menu, menu);
        MenuItem item = menu.findItem(R.id.action_list);
        if (item != null) {
            item.setIcon(!enableGrid ? R.drawable.ic_view_quilt_white_24dp : R.drawable.ic_view_list_white_24dp);
            item.setTitle(!enableGrid ? getString(R.string.grid_view) : getString(R.string.list_view));
        }
        if (viewModel.notes.getValue() != null && viewModel.notes.getValue().size() > 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete_all));
        }
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

    private void shareNote(Note note) {
        showProgress();
        BackupTool.CreateCallback callback = file -> sendNote(note, file);
        new Thread(() -> BackupTool.getInstance().createNote(note, callback)).start();
    }

    private void sendNote(Note note, File file) {
        hideProgress();
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(getContext(), getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyUtil.sendNote(file, getContext(), note.getSummary());
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
                new SyncNotes(getContext(), null).execute();
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
                mAdapter.notifyDataSetChanged();
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(NotesViewModel.class);
        viewModel.notes.observe(this, list -> {
            if (list != null) {
                filterController.setOriginal(list);
            }
        });
    }

    private void initList() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        enableGrid = getPrefs().isNotesGridEnabled();
        if (enableGrid) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }
        binding.recyclerView.setLayoutManager(layoutManager);
        mAdapter = new NotesRecyclerAdapter();
        mAdapter.setActionsListener((view, position, note, actions) -> {
            switch (actions) {
                case OPEN:
                    previewNote(note.getKey(), view);
                    break;
                case MORE:
                    showMore(view, note);
                    break;
            }
        });
        binding.recyclerView.setAdapter(mAdapter);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        refreshView();
    }

    private void showMore(View view, Note note) {
        String showIn = getString(R.string.show_in_status_bar);
        showIn = showIn.substring(0, showIn.length() - 1);
        final String[] items = {getString(R.string.open), getString(R.string.share),
                showIn, getString(R.string.change_color), getString(R.string.edit),
                getString(R.string.delete)};
        Dialogues.showLCAM(getContext(), item -> {
            switch (item) {
                case 0:
                    previewNote(note.getKey(), view);
                    break;
                case 1:
                    shareNote(note);
                    break;
                case 2:
                    showInStatusBar(note);
                    break;
                case 3:
                    selectColor(note);
                    break;
                case 4:
                    getContext().startActivity(new Intent(getContext(), CreateNoteActivity.class)
                            .putExtra(Constants.INTENT_ID, note.getKey()));
                    break;
                case 5:
                    viewModel.deleteNote(note);
                    break;
            }
        }, items);
    }

    private void showDialog() {
        final CharSequence[] items = {getString(R.string.by_date_az),
                getString(R.string.by_date_za),
                getString(R.string.name_az),
                getString(R.string.name_za)};
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
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
            viewModel.reload();
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
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setCancelable(true);
        builder.setMessage(R.string.delete_all_notes);
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteAll();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAll() {
        viewModel.deleteAll(viewModel.notes.getValue());
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

    private void showInStatusBar(Note note) {
        if (note != null) {
            new Notifier(getContext()).showNoteNotification(note);
        }
    }

    private void selectColor(Note note) {
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
            note.setColor(item);
            viewModel.saveNote(note);
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

    @Override
    public void onChanged(@NonNull List<Note> result) {
        mAdapter.setData(result);
        binding.recyclerView.smoothScrollToPosition(0);
        refreshView();
    }
}
