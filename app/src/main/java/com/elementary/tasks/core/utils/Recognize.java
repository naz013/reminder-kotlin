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

package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.backdoor.simpleai.Model;
import com.backdoor.simpleai.RecUtils;
import com.backdoor.simpleai.Recognizer;
import com.backdoor.simpleai.Types;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.SplashScreenActivity;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.birthdays.AddBirthdayActivity;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.dialogs.SelectVolume;
import com.cray.software.justreminder.dialogs.VoiceHelp;
import com.cray.software.justreminder.dialogs.VoiceResult;
import com.cray.software.justreminder.feedback.SendReportActivity;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.reminder.AddReminderActivity;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.json.JAction;
import com.cray.software.justreminder.reminder.json.JExport;
import com.cray.software.justreminder.reminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.settings.SettingsActivity;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

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
        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        String language = Language.getLanguage(prefs.getInt(Prefs.VOICE_LOCALE));
        for (Object key : matches){
            String keyStr = key.toString();
            Log.d(Constants.LOG_TAG, "Key " + keyStr);
            String morning = prefs.getString(Prefs.TIME_MORNING);
            String day = prefs.getString(Prefs.TIME_DAY);
            String evening = prefs.getString(Prefs.TIME_EVENING);
            String night = prefs.getString(Prefs.TIME_NIGHT);

            Model model = new Recognizer(mContext, new String[]{morning, day, evening, night}).parseResults(keyStr, language);
            if (model != null){
                Types types = model.getTypes();
                if (types == Types.ACTION && isWidget) {
                    int action = model.getActivity();
                    if (action == RecUtils.APP)
                        mContext.startActivity(new Intent(mContext, SplashScreenActivity.class));
                    else if (action == RecUtils.SETTINGS)
                        mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                    else if (action == RecUtils.REPORT)
                        mContext.startActivity(new Intent(mContext, SendReportActivity.class));
                    else if (action == RecUtils.HELP)
                        mContext.startActivity(new Intent(mContext, VoiceHelp.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    else if (action == RecUtils.BIRTHDAY)
                        mContext.startActivity(new Intent(mContext, AddBirthdayActivity.class));
                    else if (action == RecUtils.REMINDER)
                        mContext.startActivity(new Intent(mContext, AddReminderActivity.class));
                    else mContext.startActivity(new Intent(mContext, SelectVolume.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
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
        ArrayList<Integer> weekdays = model.getWeekdays();
        boolean isCalendar = model.getCalendar();
        long startTime = model.getDateTime();

        if (type.matches(Recognizer.WEEK)) {
            startTime = TimeCount.getNextWeekdayTime(startTime, weekdays, 0);
            if (number != null && !number.matches("")) {
                if (telephony == 1) type = Constants.TYPE_WEEKDAY_CALL;
                else type = Constants.TYPE_WEEKDAY_MESSAGE;
            }
        }

        String categoryId = GroupHelper.getInstance(mContext).getDefaultUuId();
        JRecurrence jRecurrence = new JRecurrence(0, repeat, -1, weekdays, 0);
        JAction jAction = new JAction(type, number, -1, "", null);

        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        boolean isCal = prefs.getBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = prefs.getBoolean(Prefs.EXPORT_TO_STOCK);
        int exp = (isCalendar && (isCal || isStock)) ? 1 : 0;
        JExport jExport = new JExport(0, exp, null);

        Log.d("----RECORD_TIME-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d("----EVENT_TIME-----", TimeUtil.getFullDateTime(startTime, true));

        JsonModel jsonModel = new JsonModel(summary, type, categoryId,
                SyncHelper.generateID(), startTime, startTime, jRecurrence, jAction, jExport);
        long remId = new DateType(mContext, Constants.TYPE_REMINDER).save(new ReminderItem(jsonModel));
        if (isCalendar || isStock) {
            ReminderUtils.exportToCalendar(mContext, summary, startTime, remId, isCalendar, isStock);
        }

        if (widget && !isWear) {
            mContext.startActivity(new Intent(mContext, VoiceResult.class)
                    .putExtra("ids", remId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote(String note) {
        SharedPrefs prefs = SharedPrefs.getInstance(mContext);
        Calendar calendar1 = Calendar.getInstance();
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        int month = calendar1.get(Calendar.MONTH);
        int year = calendar1.get(Calendar.YEAR);
        String date = day + "-" + month + "-" + year;
        String uuID = SyncHelper.generateID();
        int color = new Random().nextInt(15);
        if (prefs.getBoolean(Prefs.NOTE_ENCRYPT)){
            note = SyncHelper.encrypt(note);
        }
        NoteItem item = new NoteItem(note, uuID, date, color, 5, null, 0, 0);
        long remId = 0;
        if (prefs.getBoolean(Prefs.QUICK_NOTE_REMINDER)){
            String categoryId = GroupHelper.getInstance(mContext).getDefaultUuId();
            long after = prefs.getInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
            long due = calendar1.getTimeInMillis() + after;
            JRecurrence jRecurrence = new JRecurrence(0, 0, -1, null, after);
            JsonModel jsonModel = new JsonModel(note, Constants.TYPE_REMINDER, categoryId,
                    SyncHelper.generateID(), due, due, jRecurrence, null, null);
            remId = new DateType(mContext, Constants.TYPE_REMINDER).save(new ReminderItem(jsonModel));
        }
        item.setLinkId(remId);
        NoteHelper.getInstance(mContext).saveNote(item);
        UpdatesHelper.getInstance(mContext).updateNotesWidget();
        if (!isWear) Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }
}