package com.elementary.tasks.places.list;

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
import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.view_models.places.PlacesViewModel;
import com.elementary.tasks.databinding.FragmentPlacesBinding;
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment;
import com.elementary.tasks.places.create.CreatePlaceActivity;
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
public class PlacesFragment extends BaseNavigationFragment implements FilterCallback<Place> {

    private FragmentPlacesBinding binding;
    private PlacesViewModel viewModel;

    @NonNull
    private PlacesRecyclerAdapter mAdapter = new PlacesRecyclerAdapter();
    @Nullable
    private SearchView mSearchView = null;
    @Nullable
    private MenuItem mSearchMenu = null;

    @NonNull
    private PlaceFilterController filterController = new PlaceFilterController(this);

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            filterController.setSearchValue(query);
            if (mSearchMenu != null) {
                mSearchMenu.collapseActionView();
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            filterController.setSearchValue(newText);
            return false;
        }
    };
    private SearchView.OnCloseListener mSearchCloseListener = () -> false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.archive_menu, menu);
        menu.findItem(R.id.action_delete_all).setVisible(false);
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
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList();
        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel.class);
        viewModel.places.observe(this, places -> {
            if (places != null) {
                filterController.setOriginal(places);
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.places));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> startActivity(new Intent(getContext(), CreatePlaceActivity.class)));
            getCallback().onScrollChanged(binding.recyclerView);
        }
    }

    private void initList() {
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter.setActionsListener((view, position, place, actions) -> {
            switch (actions) {
                case OPEN:
                    if (place != null) openPlace(place);
                    break;
                case MORE:
                    showMore(place);
                    break;
            }
        });
        binding.recyclerView.setAdapter(mAdapter);
        refreshView();
    }

    private void showMore(Place place) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getContext(), item -> {
            if (item == 0) {
                openPlace(place);
            } else if (item == 1) {
                viewModel.deletePlace(place);
            }
        }, items);
    }

    private void openPlace(Place place) {
        startActivity(new Intent(getContext(), CreatePlaceActivity.class)
                .putExtra(Constants.INTENT_ID, place.getId()));
    }

    private void refreshView() {
        if (mAdapter.getItemCount() == 0) {
            binding.emptyItem.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyItem.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChanged(@NonNull List<Place> result) {
        mAdapter.setData(result);
        binding.recyclerView.smoothScrollToPosition(0);
        refreshView();
    }
}
