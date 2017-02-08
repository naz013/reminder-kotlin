package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemTaskBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder> {

    private List<TaskItem> mDataset;
    private Context mContext;
    private static TasksCallback listener;
    private static Map<String, Integer> colors;

    public TasksRecyclerAdapter(Context context, List<TaskItem> myDataset, Map<String, Integer> colors) {
        this.mDataset = myDataset;
        this.mContext = context;
        TasksRecyclerAdapter.colors = colors;
    }

    public void setListener(TasksCallback listener) {
        TasksRecyclerAdapter.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListItemTaskBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.setClick(v1 -> onItemClick(getAdapterPosition()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(ListItemTaskBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TaskItem item = mDataset.get(position);
        holder.binding.setTask(item);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void onItemClick(int position) {
        mContext.startActivity(new Intent(mContext, TaskActivity.class)
                .putExtra(Constants.INTENT_ID, mDataset.get(position).getTaskId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
    }

    private static void switchTask(Context context, boolean isDone, String listId, String taskId) {
        RealmDb.getInstance().setStatus(taskId, isDone);
        new SwitchTaskAsync(context, listId, taskId, isDone, listener).execute();
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(View view, String listId) {
        if (colors.containsKey(listId)) {
            view.setBackgroundColor(new ThemeUtil(view.getContext()).getNoteColor(colors.get(listId)));
        }
    }

    @BindingAdapter({"loadTaskCard"})
    public static void loadTaskCard(CardView cardView, int i) {
        cardView.setCardBackgroundColor(new ThemeUtil(cardView.getContext()).getCardStyle());
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboCheckBox checkBox, TaskItem item) {
        if (item.getStatus().matches(Google.TASKS_COMPLETE)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> switchTask(checkBox.getContext(),
                isChecked, item.getListId(), item.getTaskId()));
    }

    @BindingAdapter({"loadDue"})
    public static void loadDue(RoboTextView view, long due) {
        SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        if (due != 0) {
            calendar.setTimeInMillis(due);
            String update = full24Format.format(calendar.getTime());
            view.setText(update);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }
}
