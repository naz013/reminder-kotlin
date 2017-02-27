package com.elementary.tasks.navigation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.FragmentGroupsBinding;
import com.elementary.tasks.groups.CreateGroupActivity;
import com.elementary.tasks.groups.GroupsRecyclerAdapter;

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

public class GroupsFragment extends BaseNavigationFragment {

    private FragmentGroupsBinding binding;
    private GroupsRecyclerAdapter mAdapter;
    private SimpleListener mEventListener = new SimpleListener() {
        @Override
        public void onItemClicked(int position, View view) {
            startActivity(new Intent(getContext(), CreateGroupActivity.class).putExtra(Constants.INTENT_ID, mAdapter.getItem(position).getUuId()));
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            String[] items = {getString(R.string.change_color), getString(R.string.edit), getString(R.string.delete)};
            if (mAdapter.getItemCount() == 1) items = new String[]{getString(R.string.change_color), getString(R.string.edit)};
            Dialogues.showLCAM(getContext(), item -> {
                switch (item){
                    case 0:
                        changeColor(mAdapter.getItem(position).getUuId());
                        break;
                    case 1:
                        startActivity(new Intent(getContext(), CreateGroupActivity.class)
                                .putExtra(Constants.INTENT_ID, mAdapter.getItem(position).getUuId()));
                        break;
                    case 2:
                        mAdapter.deleteItem(position);
                        refreshView();
                        break;
                }
            }, items);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        initGroupsList();
        return binding.getRoot();
    }

    private void changeColor(final String id) {
        String[] items = {getString(R.string.red), getString(R.string.purple),
                getString(R.string.green), getString(R.string.green_light),
                getString(R.string.blue), getString(R.string.blue_light),
                getString(R.string.yellow), getString(R.string.orange),
                getString(R.string.cyan), getString(R.string.pink),
                getString(R.string.teal), getString(R.string.amber)};
        if (Module.isPro()){
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
            RealmDb.getInstance().changeGroupColor(id, item);
            showTemplates();
        }, items);
    }

    private void initGroupsList() {
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.groups));
            getCallback().onFragmentSelect(this);
            getCallback().setClick(view -> startActivity(new Intent(getContext(), CreateGroupActivity.class)));
            getCallback().onScrollChanged(binding.recyclerView);
        }
        showTemplates();
    }

    private void showTemplates() {
        mAdapter = new GroupsRecyclerAdapter(getContext(), RealmDb.getInstance().getAllGroups(), mEventListener);
        binding.recyclerView.setAdapter(mAdapter);
        refreshView();
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
