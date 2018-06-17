package com.elementary.tasks.google_tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.data.models.GoogleTask;
import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ListItemTaskBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

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

    private List<GoogleTask> googleTasks = new ArrayList<>();
    @Nullable
    private ActionsListener<GoogleTask> actionsListener;

    TasksRecyclerAdapter() {
    }

    public void setGoogleTasks(List<GoogleTask> googleTasks) {
        this.googleTasks = googleTasks;
        notifyDataSetChanged();
    }

    public void setActionsListener(@Nullable ActionsListener<GoogleTask> actionsListener) {
        this.actionsListener = actionsListener;
    }

    @Nullable
    public ActionsListener<GoogleTask> getActionsListener() {
        return actionsListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListItemTaskBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.setClick(v1 -> onItemClick(getAdapterPosition()));
            binding.checkDone.setOnClickListener(view -> switchTask(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        GoogleTask item = googleTasks.get(position);
        holder.binding.setTaskItem(item);
    }

    @Override
    public int getItemCount() {
        return googleTasks.size();
    }

    private void onItemClick(int position) {
        if (getActionsListener() != null) {
            getActionsListener().onAction(null, position, googleTasks.get(position), ListActions.EDIT);
        }
    }

    private void switchTask(int position) {
        if (getActionsListener() != null) {
            getActionsListener().onAction(null, position, googleTasks.get(position), ListActions.SWITCH);
        }
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(View view, String listId) {
//        if (listId != null && colors != null && colors.containsKey(listId)) {
//            view.setBackgroundColor(ThemeUtil.getInstance(view.getContext()).getNoteColor(colors.get(listId)));
//        }
    }

    @BindingAdapter({"loadTaskCard"})
    public static void loadTaskCard(CardView cardView, int i) {
        cardView.setCardBackgroundColor(ThemeUtil.getInstance(cardView.getContext()).getCardStyle());
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboCheckBox checkBox, GoogleTask item) {
        if (item.getStatus().matches(Google.TASKS_COMPLETE)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
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
