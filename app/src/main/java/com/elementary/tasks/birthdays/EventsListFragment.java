package com.elementary.tasks.birthdays;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentEventsListBinding;
import com.elementary.tasks.navigation.fragments.BaseFragment;
import com.elementary.tasks.navigation.fragments.DayViewFragment;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.ReminderPreviewActivity;
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

public class EventsListFragment extends BaseFragment implements RecyclerListener {

    private FragmentEventsListBinding binding;
    private List<EventsItem> mDataList = new ArrayList<>();
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsListBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        if (getCallback() != null) {
            getCallback().onScrollChanged(binding.recyclerView);
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdapter();
    }

    public void loadAdapter(){
        CalendarEventsAdapter mAdapter = new CalendarEventsAdapter(getContext(), mDataList);
        mAdapter.setEventListener(this);
        binding.recyclerView.setAdapter(mAdapter);
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

    private void showBirthdayLcam(BirthdayItem birthdayItem, int position) {
        String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getContext(), item -> {
            switch (item){
                case 0:
                    editBirthday(birthdayItem);
                    break;
                case 1:
                    RealmDb.getInstance().deleteBirthday(birthdayItem);
                    new DeleteBirthdayFilesAsync(getContext()).execute(birthdayItem.getUuId());
                    reopenFragment();
                    break;
            }
        }, items);
    }

    private void reopenFragment() {
        getCallback().replaceFragment(new DayViewFragment(), getString(R.string.events));;
    }

    private void editBirthday(BirthdayItem item) {
        startActivity(new Intent(getContext(), AddBirthdayActivity.class)
                .putExtra(Constants.INTENT_ID, item.getKey()));
    }

    @Override
    public void onItemClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof BirthdayItem) {
            editBirthday((BirthdayItem) object);
        } else if (object instanceof Reminder){
            showReminder((Reminder) object);
        }
    }

    private void showReminder(Reminder object) {
        if (Reminder.isSame(object.getType(), Reminder.BY_DATE_SHOP)){
            getContext().startActivity(new Intent(getContext(), ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUuId()));
        } else {
            getContext().startActivity(new Intent(getContext(), ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUuId()));
        }
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(getContext(), CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void showActionDialog(Reminder reminder, int position) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.move_to_trash)};
        Dialogues.showLCAM(getContext(), item -> {
            switch (item){
                case 0:
                    showReminder(reminder);
                    break;
                case 1:
                    editReminder(reminder.getUuId());
                    break;
                case 3:
                    EventControl control = EventControlFactory.getController(getContext(), reminder.setRemoved(true));
                    control.stop();
                    reopenFragment();
                    break;
            }
        }, items);
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof BirthdayItem) {
            showBirthdayLcam((BirthdayItem) object, position);
        } else if (object instanceof Reminder) {
            showActionDialog((Reminder) object, position);
        }
    }

    @Override
    public void onItemSwitched(int position, View view) {

    }
}
