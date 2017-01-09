package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.backdoor.simpleai.Model;
import com.backdoor.simpleai.RecUtils;
import com.backdoor.simpleai.Recognizer;
import com.backdoor.simpleai.Types;
import com.elementary.tasks.R;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

public class Recognize {

    private Context mContext;
    private boolean isWear;

    public Recognize(Context context){
        this.mContext = context;
    }

    public void parseResults(ArrayList matches, boolean isWidget, boolean isWear){
        this.isWear = isWear;
        this.parseResults(matches, isWidget);
    }

    public void parseResults(ArrayList matches, boolean isWidget){
        Prefs prefs = Prefs.getInstance(mContext);
        String language = Language.getLanguage(prefs.getVoiceLocale());
        for (Object key : matches){
            String keyStr = key.toString();
            String morning = prefs.getMorningTime();
            String day = prefs.getNoonTime();
            String evening = prefs.getEveningTime();
            String night = prefs.getNightTime();
            Model model = new Recognizer(mContext, new String[]{morning, day, evening, night}).parseResults(keyStr, language);
            if (model != null){
                Types types = model.getTypes();
                if (types == Types.ACTION && isWidget) {
                    int action = model.getActivity();
                    if (action == RecUtils.APP)
                        mContext.startActivity(new Intent(mContext, SplashScreen.class));
//                    else if (action == RecUtils.REPORT)
//                        mContext.startActivity(new Intent(mContext, SendReportActivity.class));
//                    else if (action == RecUtils.HELP)
//                        mContext.startActivity(new Intent(mContext, VoiceHelp.class)
//                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
//                    else if (action == RecUtils.BIRTHDAY)
//                        mContext.startActivity(new Intent(mContext, AddBirthdayActivity.class));
//                    else if (action == RecUtils.REMINDER)
//                        mContext.startActivity(new Intent(mContext, AddReminderActivity.class));
//                    else mContext.startActivity(new Intent(mContext, SelectVolume.class)
//                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                } else if (types == Types.NOTE) {
                    saveNote(model.getSummary());
                } else if (types == Types.REMINDER) {
                    saveReminder(model, isWidget);
                }
                break;
            }
        }
    }

    private void saveReminder(Model model, boolean widget) {
        String type = model.getType();
        String number = model.getNumber();
        String summary = model.getSummary();
        long repeat = model.getRepeat();
        int telephony = model.getAction();
        List<Integer> weekdays = model.getWeekdays();
        boolean isCalendar = model.getCalendar();
        long startTime = model.getDateTime();
        int typeT = Reminder.BY_DATE;
        if (type.matches(Recognizer.WEEK)) {
            typeT = Reminder.BY_WEEK;
            startTime = TimeCount.getInstance(mContext).getNextWeekdayTime(startTime, weekdays, 0);
            if (!TextUtils.isEmpty(number)) {
                if (telephony == 1) typeT = Reminder.BY_WEEK_CALL;
                else typeT = Reminder.BY_WEEK_SMS;
            }
        }
        String categoryId = RealmDb.getInstance().getDefaultGroup().getUuId();
        Prefs prefs = Prefs.getInstance(mContext);
        boolean isCal = prefs.getBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = prefs.getBoolean(Prefs.EXPORT_TO_STOCK);
        Log.d("----EVENT_TIME-----", TimeUtil.getFullDateTime(startTime, true, true));
        Reminder reminder = new Reminder();
        reminder.setType(typeT);
        reminder.setSummary(summary);
        reminder.setGroupUuId(categoryId);
        reminder.setWeekdays(weekdays);
        reminder.setRepeatInterval(repeat);
        reminder.setTarget(number);
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(startTime));
        reminder.setExportToCalendar(isCalendar && (isCal || isStock));
        EventControl control = EventControlImpl.getController(mContext, reminder);
        control.start();
        if (widget && !isWear) {
//            mContext.startActivity(new Intent(mContext, VoiceResult.class)
//                    .putExtra("ids", remId)
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote(String note) {
        Prefs prefs = Prefs.getInstance(mContext);
        int color = new Random().nextInt(15);
        NoteItem item = new NoteItem();
        item.setColor(color);
        item.setSummary(note);
        item.setDate(TimeUtil.getGmtDateTime());
        if (prefs.getBoolean(Prefs.QUICK_NOTE_REMINDER)){
            long after = prefs.getInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
            long due = System.currentTimeMillis() + after;
            Reminder mReminder = new Reminder();
            mReminder.setType(Reminder.BY_DATE);
            mReminder.setDelay(0);
            mReminder.setEventCount(0);
            mReminder.setUseGlobal(true);
            mReminder.setNoteId(item.getKey());
            mReminder.setSummary(item.getSummary());
            mReminder.setGroupUuId(RealmDb.getInstance().getDefaultGroup().getUuId());
            mReminder.setStartTime(TimeUtil.getGmtFromDateTime(due));
            mReminder.setEventTime(TimeUtil.getGmtFromDateTime(due));
            RealmDb.getInstance().saveObject(mReminder);
            EventControl control = EventControlImpl.getController(mContext, mReminder);
            control.start();
        }
        RealmDb.getInstance().saveObject(item);
//        UpdatesHelper.getInstance(mContext).updateNotesWidget();
        if (!isWear) Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }
}