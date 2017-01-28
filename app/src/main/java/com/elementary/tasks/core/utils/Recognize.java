package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import com.backdoor.simpleai.Action;
import com.backdoor.simpleai.ActionType;
import com.backdoor.simpleai.ContactOutput;
import com.backdoor.simpleai.ContactsInterface;
import com.backdoor.simpleai.Model;
import com.backdoor.simpleai.Recognizer;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.dialogs.VoiceHelpDialog;
import com.elementary.tasks.core.dialogs.VoiceResultDialog;
import com.elementary.tasks.core.dialogs.VolumeDialog;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.AddReminderActivity;
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

    private static final String TAG = "Recognize";

    private Context mContext;
    private boolean isWear;

    public Recognize(Context context) {
        this.mContext = context;
    }

    public Model findResults(ArrayList matches) {
        Prefs prefs = Prefs.getInstance(mContext);
        String language = Language.getLanguage(prefs.getVoiceLocale());
        String morning = prefs.getMorningTime();
        String day = prefs.getNoonTime();
        String evening = prefs.getEveningTime();
        String night = prefs.getNightTime();
        String[] times = new String[]{morning, day, evening, night};
        Recognizer recognizer = new Recognizer.Builder()
                .with(mContext)
                .setLocale(language)
                .setTimes(times)
                .setContactsInterface(new ContactHelper())
                .build();
        for (int i = 0; i < matches.size(); i++) {
            Object key = matches.get(i);
            String keyStr = key.toString();
            Model model = recognizer.parse(keyStr);
            if (model != null) {
                LogUtil.d(TAG, "parseResults: " + model);
                return model;
            }
        }
        return null;
    }

    public void parseResults(ArrayList matches, boolean isWidget, boolean isWear) {
        this.isWear = isWear;
        this.parseResults(matches, isWidget);
    }

    public void parseResults(ArrayList matches, boolean isWidget) {
        Prefs prefs = Prefs.getInstance(mContext);
        String language = Language.getLanguage(prefs.getVoiceLocale());
        String morning = prefs.getMorningTime();
        String day = prefs.getNoonTime();
        String evening = prefs.getEveningTime();
        String night = prefs.getNightTime();
        String[] times = new String[]{morning, day, evening, night};
        Recognizer recognizer = new Recognizer.Builder()
                .with(mContext)
                .setLocale(language)
                .setTimes(times)
                .setContactsInterface(new ContactHelper())
                .build();
        for (int i = 0; i < matches.size(); i++) {
            Object key = matches.get(i);
            String keyStr = key.toString();
            Model model = recognizer.parse(keyStr);
            if (model != null) {
                LogUtil.d(TAG, "parseResults: " + model);
                ActionType types = model.getType();
                if (types == ActionType.ACTION && isWidget) {
                    Action action = model.getAction();
                    if (action == Action.APP) {
                        mContext.startActivity(new Intent(mContext, SplashScreen.class));
                    } else if (action == Action.HELP) {
                        mContext.startActivity(new Intent(mContext, VoiceHelpDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    } else if (action == Action.BIRTHDAY) {
                        mContext.startActivity(new Intent(mContext, AddBirthdayActivity.class));
                    } else if (action == Action.REMINDER) {
                        mContext.startActivity(new Intent(mContext, AddReminderActivity.class));
                    } else if (action == Action.VOLUME) {
                        mContext.startActivity(new Intent(mContext, VolumeDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    }
                } else if (types == ActionType.NOTE) {
                    saveNote(model.getSummary());
                } else if (types == ActionType.REMINDER) {
                    saveReminder(model, isWidget);
                }
                break;
            }
        }
        Toast.makeText(mContext, R.string.failed_to_recognize_your_command, Toast.LENGTH_SHORT).show();
    }

    private void saveReminder(Model model, boolean widget) {
        Action action = model.getAction();
        String number = model.getTarget();
        String summary = model.getSummary();
        long repeat = model.getRepeatInterval();
        List<Integer> weekdays = model.getWeekdays();
        boolean isCalendar = model.isHasCalendar();
        long startTime = model.getDateTime();
        int typeT = Reminder.BY_DATE;
        if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
            typeT = Reminder.BY_WEEK;
            startTime = TimeCount.getInstance(mContext).getNextWeekdayTime(startTime, weekdays, 0);
            if (!TextUtils.isEmpty(number)) {
                if (action == Action.WEEK_CALL) typeT = Reminder.BY_WEEK_CALL;
                else typeT = Reminder.BY_WEEK_SMS;
            }
        } else if (action == Action.CALL) {
            typeT = Reminder.BY_DATE_CALL;
        } else if (action == Action.MESSAGE) {
            typeT = Reminder.BY_DATE_SMS;
        } else if (action == Action.MAIL) {
            typeT = Reminder.BY_DATE_EMAIL;
        }
        String categoryId = RealmDb.getInstance().getDefaultGroup().getUuId();
        Prefs prefs = Prefs.getInstance(mContext);
        boolean isCal = prefs.getBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = prefs.getBoolean(Prefs.EXPORT_TO_STOCK);
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
            mContext.startActivity(new Intent(mContext, VoiceResultDialog.class)
                    .putExtra(Constants.INTENT_ID, reminder.getUuId())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
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
        if (prefs.getBoolean(Prefs.QUICK_NOTE_REMINDER)) {
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
        UpdatesHelper.getInstance(mContext).updateNotesWidget();
        if (!isWear)
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

    private class ContactHelper implements ContactsInterface {

        @Override
        public ContactOutput findEmail(String input, Context context) {
            String number = null;
            String[] parts = input.split("\\s");
            for (String part : parts) {
                while (part.length() > 1) {
                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Email.DATA};
                    Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, selection, null, null);
                    if (c != null && c.moveToFirst()) {
                        number = c.getString(0);
                        c.close();
                    }
                    if (number != null)
                        break;
                    part = part.substring(0, part.length() - 2);
                }
                if (number != null) {
                    input = input.replace(part, "");
                    break;
                }
            }
            return new ContactOutput(input, number);
        }

        @Override
        public ContactOutput findNumber(String input, Context context) {
            String number = null;
            String[] parts = input.split("\\s");
            for (String part : parts) {
                while (part.length() > 1) {
                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, selection, null, null);
                    if (c != null && c.moveToFirst()) {
                        number = c.getString(0);
                        c.close();
                    }
                    if (number != null) {
                        break;
                    }
                    part = part.substring(0, part.length() - 1);
                }
                if (number != null) {
                    input = input.replace(part, "");
                    break;
                }
            }
            return new ContactOutput(input.trim(), number);
        }
    }
}