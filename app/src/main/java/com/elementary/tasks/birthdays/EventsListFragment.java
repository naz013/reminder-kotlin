package com.elementary.tasks.birthdays;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.databinding.FragmentEventsListBinding;
import com.elementary.tasks.reminder.RecyclerListener;

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

public class EventsListFragment extends Fragment implements RecyclerListener {

    private FragmentEventsListBinding binding;
    private List<EventsItem> mDataList = new ArrayList<>();
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private Context mContext;

    public void setData(List<EventsItem> datas){
        this.mDataList = new ArrayList<>(datas);
    }

    public static EventsListFragment newInstance(int page) {
        EventsListFragment pageFragment = new EventsListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsListBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loaderAdapter();
    }

    public void loaderAdapter(){
        CalendarEventsAdapter customAdapter = new CalendarEventsAdapter(mContext, mDataList);
        customAdapter.setEventListener(this);
        binding.recyclerView.setAdapter(customAdapter);
        reloadView();
    }

    private void reloadView() {
        int size = mDataList != null ? mDataList.size() : 0;
        if (size > 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClicked(int position, View view) {
        Object object = mDataList.get(position);
        if (object instanceof BirthdayItem) {
//            startActivity(new Intent(mContext, AddBirthdayActivity.class)
//                    .putExtra("BDid", ((BirthdayItem) object).getId())
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
//            Reminder.edit(((ReminderItem) object).getId(), mContext);
        }
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        Object object = mDataList.get(position);
        if (object instanceof BirthdayItem) {
//            BirthdayHelper.getInstance(mContext).deleteBirthday(((BirthdayItem) object).getId());
//            mDataList.remove(position);
//            loaderAdapter();
//            Messages.toast(mContext, getString(R.string.deleted));
        }
    }

    @Override
    public void onItemSwitched(int position, View view) {

    }
}
