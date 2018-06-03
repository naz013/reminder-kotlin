package com.elementary.tasks.birthdays;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.create_edit.AddBirthdayActivity;
import com.elementary.tasks.birthdays.work.DeleteBirthdayFilesAsync;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentEventsListBinding;
import com.elementary.tasks.navigation.fragments.BaseFragment;
import com.elementary.tasks.navigation.fragments.DayViewFragment;
import com.elementary.tasks.reminder.lists.RecyclerListener;
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity;
import com.elementary.tasks.reminder.preview.ShoppingPreviewActivity;
import com.elementary.tasks.core.data.models.Reminder;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import timber.log.Timber;

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
public class EventsListFragment extends BaseFragment implements RecyclerListener,
        DayViewProvider.Callback, DayViewProvider.InitCallback {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page";

    private FragmentEventsListBinding binding;
    private List<EventsItem> mDataList = new ArrayList<>();
    @Nullable
    private EventsPagerItem mItem;

    public static EventsListFragment newInstance(EventsPagerItem item) {
        EventsListFragment pageFragment = new EventsListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_PAGE_NUMBER, item);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItem = (EventsPagerItem) getArguments().getSerializable(ARGUMENT_PAGE_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsListBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        if (getCallback() != null) {
            getCallback().onScrollChanged(binding.recyclerView);
        }
        reloadView();
        DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
        if (provider != null) {
            provider.addObserver(this);
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
        if (provider != null) {
            provider.removeObserver(this);
            provider.removeCallback(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdapter();
    }

    private void loadData() {
        DayViewProvider provider = EventsDataSingleton.getInstance().getProvider();
        if (provider != null && mItem != null) {
            provider.findMatches(mItem.getDay(), mItem.getMonth(), mItem.getYear(), true, this);
        }
    }

    public void loadAdapter() {
        CalendarEventsAdapter mAdapter = new CalendarEventsAdapter(getContext(), mDataList);
        mAdapter.setEventListener(this);
        binding.recyclerView.setAdapter(mAdapter);
        reloadView();
    }

    private void reloadView() {
        int size = mDataList != null ? mDataList.size() : 0;
        if (size > 0) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void showBirthdayLcam(Birthday birthday) {
        String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getContext(), item -> {
            switch (item) {
                case 0:
                    editBirthday(birthday);
                    break;
                case 1:
                    RealmDb.getInstance().deleteBirthday(birthday);
                    new DeleteBirthdayFilesAsync(getContext()).execute(birthday.getUuId());
                    reopenFragment();
                    break;
            }
        }, items);
    }

    private void reopenFragment() {
        getCallback().replaceFragment(new DayViewFragment(), getString(R.string.events));
    }

    private void editBirthday(Birthday item) {
        startActivity(new Intent(getContext(), AddBirthdayActivity.class)
                .putExtra(Constants.INTENT_ID, item.getKey()));
    }

    @Override
    public void onItemClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof Birthday) {
            editBirthday((Birthday) object);
        } else if (object instanceof Reminder) {
            if (view.getId() == R.id.button_more) {
                showActionDialog((Reminder) object, view);
            } else {
                showReminder((Reminder) object);
            }
        }
    }

    private void showReminder(Reminder object) {
        if (Reminder.isSame(object.getType(), Reminder.BY_DATE_SHOP)) {
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

    private void showActionDialog(@NonNull Reminder reminder, View view) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.move_to_trash)};
        Dialogues.showPopup(getContext(), view, item -> {
            switch (item) {
                case 0:
                    showReminder(reminder);
                    break;
                case 1:
                    editReminder(reminder.getUuId());
                    break;
                case 3:
                    EventControl control = EventControlFactory.getController(reminder.setRemoved(true));
                    control.stop();
                    reopenFragment();
                    break;
            }
        }, items);
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof Birthday) {
            showBirthdayLcam((Birthday) object);
        }
    }

    @Override
    public void onItemSwitched(int position, View view) {

    }

    @Override
    public void apply(@NonNull List<EventsItem> list) {
        Timber.d("apply: %d, %s", list.size(), mItem);
        this.mDataList.clear();
        this.mDataList.addAll(list);
        if (isResumed()) loadAdapter();
    }

    @Override
    public void onFinish() {
        loadData();
    }
}
