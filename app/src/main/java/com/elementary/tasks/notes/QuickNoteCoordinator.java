package com.elementary.tasks.notes;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.ActivityMainBinding;
import com.elementary.tasks.databinding.NoteInputCardBinding;
import com.elementary.tasks.databinding.NoteReminderCardBinding;
import com.elementary.tasks.databinding.NoteStatusCardBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.reminder.ReminderUpdateEvent;
import com.elementary.tasks.reminder.models.Reminder;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;

/**
 * Copyright 2017 Nazar Suhovich
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

public class QuickNoteCoordinator {

    private Context mContext;
    private ActivityMainBinding binding;
    private Callback mCallback;
    private ThemeUtil themeUtil;

    public QuickNoteCoordinator(Context context, ActivityMainBinding binding, Callback callback) {
        this.binding = binding;
        this.mContext = context;
        this.mCallback = callback;
        this.themeUtil = ThemeUtil.getInstance(context);
        this.binding.quickNoteContainer.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (isNoteVisible()) {
                    hideNoteView();
                }
                return true;
            }
            return false;
        });
        this.binding.quickNoteContainer.setVisibility(View.GONE);
    }

    public void switchQuickNote() {
        if (isNoteVisible()) {
            hideNoteView();
        } else {
            showNoteView();
        }
    }

    public boolean isNoteVisible() {
        return binding.quickNoteContainer.getVisibility() == View.VISIBLE;
    }

    public void hideNoteView() {
        ViewUtils.hideReveal(binding.quickNoteContainer);
        binding.quickNoteView.removeAllViewsInLayout();
        if (mCallback != null) {
            mCallback.onClose();
        }
    }

    private void showNoteView() {
        ViewUtils.showReveal(binding.quickNoteContainer);
        new Handler().postDelayed(this::addFirstCard, 250);
    }

    private void addFirstCard() {
        NoteInputCardBinding binding = NoteInputCardBinding.inflate(LayoutInflater.from(mContext), this.binding.quickNoteView, false);
        binding.buttonSave.setOnClickListener(view -> saveNote(binding));
        binding.noteCard.setVisibility(View.GONE);
        if (Module.isLollipop()) {
            binding.noteCard.setElevation(Configs.CARD_ELEVATION);
        }
        binding.noteCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()));
        binding.bgView.setBackgroundColor(themeUtil.getBackgroundStyle());
        this.binding.quickNoteView.addView(binding.getRoot());
        ViewUtils.slideInUp(mContext, binding.noteCard);
        if (mCallback != null) {
            mCallback.onOpen();
        }
    }

    private void saveNote(NoteInputCardBinding binding) {
        String text = binding.quickNote.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            binding.quickNote.setError(mContext.getString(R.string.must_be_not_empty));
            return;
        }
        binding.quickNote.setEnabled(false);
        binding.buttonSave.setEnabled(false);
        NoteItem item = new NoteItem();
        item.setSummary(text);
        item.setDate(TimeUtil.getGmtDateTime());
        if (Prefs.getInstance(mContext).isNoteColorRememberingEnabled()) {
            item.setColor(Prefs.getInstance(mContext).getLastNoteColor());
        } else {
            item.setColor(new Random().nextInt(16));
        }
        RealmDb.getInstance().saveObject(item);
        if (Prefs.getInstance(mContext).isNoteReminderEnabled()) {
            addReminderCard(item);
        } else {
            addNotificationCard(item);
        }
    }

    private void addReminderCard(NoteItem item) {
        NoteReminderCardBinding cardBinding = NoteReminderCardBinding.inflate(LayoutInflater.from(mContext), this.binding.quickNoteView, false);
        if (Module.isLollipop()) {
            cardBinding.noteReminderCard.setElevation(Configs.CARD_ELEVATION);
        }
        cardBinding.buttonYes.setOnClickListener(view -> {
            cardBinding.buttonNo.setEnabled(false);
            cardBinding.buttonYes.setEnabled(false);
            addReminderToNote(item);
        });
        cardBinding.buttonNo.setOnClickListener(view -> {
            cardBinding.buttonNo.setEnabled(false);
            cardBinding.buttonYes.setEnabled(false);
            addNotificationCard(item);
        });
        cardBinding.noteReminderCard.setVisibility(View.GONE);
        cardBinding.noteReminderCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()));
        cardBinding.bgView.setBackgroundColor(themeUtil.getBackgroundStyle());
        this.binding.quickNoteView.addView(cardBinding.getRoot());
        new Handler().postDelayed(() -> ViewUtils.slideInUp(mContext, cardBinding.noteReminderCard), 250);
    }

    private void addReminderToNote(NoteItem item) {
        Reminder reminder = new Reminder();
        reminder.setType(Reminder.BY_DATE);
        reminder.setDelay(0);
        reminder.setEventCount(0);
        reminder.setUseGlobal(true);
        reminder.setNoteId(item.getKey());
        reminder.setActive(true);
        reminder.setRemoved(false);
        reminder.setSummary(item.getSummary());
        GroupItem def = RealmDb.getInstance().getDefaultGroup();
        if (def != null) {
            reminder.setGroupUuId(def.getUuId());
        }
        long prefsTime = Prefs.getInstance(mContext).getNoteReminderTime() * TimeCount.MINUTE;
        long startTime = System.currentTimeMillis() + prefsTime;
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        EventControl control = EventControlFactory.getController(mContext, reminder);
        control.start();
        EventBus.getDefault().post(new ReminderUpdateEvent());
        addNotificationCard(item);
    }

    private void addNotificationCard(@NonNull NoteItem item) {
        NoteStatusCardBinding cardBinding = NoteStatusCardBinding.inflate(LayoutInflater.from(mContext), binding.quickNoteView, false);
        if (Module.isLollipop()) {
            cardBinding.noteStatusCard.setElevation(Configs.CARD_ELEVATION);
        }
        cardBinding.buttonYes.setOnClickListener(view -> {
            cardBinding.buttonNo.setEnabled(false);
            cardBinding.buttonYes.setEnabled(false);
            showInStatusBar(item);
        });
        cardBinding.buttonNo.setOnClickListener(view -> hideNoteView());
        cardBinding.noteStatusCard.setVisibility(View.GONE);
        cardBinding.noteStatusCard.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorPrimary()));
        cardBinding.bgView.setBackgroundColor(themeUtil.getBackgroundStyle());
        this.binding.quickNoteView.addView(cardBinding.getRoot());
        new Handler().postDelayed(() -> ViewUtils.slideInUp(mContext, cardBinding.noteStatusCard), 250);
    }

    private void showInStatusBar(@NonNull NoteItem item) {
        new Notifier(mContext).showNoteNotification(item);
        hideNoteView();
    }

    public interface Callback {
        void onOpen();

        void onClose();
    }
}
