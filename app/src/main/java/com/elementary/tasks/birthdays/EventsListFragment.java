package com.elementary.tasks.birthdays;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.create_edit.AddBirthdayActivity;
import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.view_models.day_view.DayViewViewModel;
import com.elementary.tasks.databinding.FragmentEventsListBinding;
import com.elementary.tasks.navigation.fragments.BaseFragment;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.reminder.lists.RecyclerListener;
import com.elementary.tasks.reminder.preview.ReminderPreviewActivity;
import com.elementary.tasks.reminder.preview.ShoppingPreviewActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
public class EventsListFragment extends BaseFragment implements RecyclerListener {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page";

    private FragmentEventsListBinding binding;
    @NonNull
    private CalendarEventsAdapter mAdapter = new CalendarEventsAdapter();
    private DayViewViewModel viewModel;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setEventListener(this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(mAdapter);
        if (getCallback() != null) {
            getCallback().onScrollChanged(binding.recyclerView);
        }

        reloadView();
        initBirthdayViewModel();
    }

    private void initBirthdayViewModel() {
        viewModel = ViewModelProviders.of(this).get(DayViewViewModel.class);
        viewModel.setItem(mItem);
        viewModel.events.observe(this, eventsItems -> {
            if (eventsItems != null) {
                mAdapter.setData(eventsItems);
                reloadView();
            }
        });
    }

    private void reloadView() {
        if (mAdapter.getItemCount() > 0) {
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
                    viewModel.deleteBirthday(birthday);
                    break;
            }
        }, items);
    }

    private void editBirthday(Birthday item) {
        startActivity(new Intent(getContext(), AddBirthdayActivity.class)
                .putExtra(Constants.INTENT_ID, item.getUniqueId()));
    }

    @Override
    public void onItemClicked(int position, View view) {
        Object object = mAdapter.getItem(position).getObject();
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
            startActivity(new Intent(getContext(), ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUniqueId()));
        } else {
            startActivity(new Intent(getContext(), ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUniqueId()));
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
                    viewModel.moveToTrash(reminder);
                    break;
            }
        }, items);
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        Object object = mAdapter.getItem(position).getObject();
        if (object instanceof Birthday) {
            showBirthdayLcam((Birthday) object);
        }
    }

    @Override
    public void onItemSwitched(int position, View view) {

    }
}
