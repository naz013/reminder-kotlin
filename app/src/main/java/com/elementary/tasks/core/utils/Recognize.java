package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.dialogs.VoiceHelpDialog;
import com.elementary.tasks.core.dialogs.VoiceResultDialog;
import com.elementary.tasks.core.dialogs.VolumeDialog;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.navigation.MainActivity;
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
    private Recognizer recognizer;

    public Recognize(Context context) {
        this.mContext = context;
        Prefs prefs = Prefs.getInstance(mContext);
        String language = Language.getLanguage(prefs.getVoiceLocale());
        String morning = prefs.getMorningTime();
        String day = prefs.getNoonTime();
        String evening = prefs.getEveningTime();
        String night = prefs.getNightTime();
        String[] times = new String[]{morning, day, evening, night};
        recognizer = new Recognizer.Builder()
                .with(mContext)
                .setLocale(language)
                .setTimes(times)
                .setContactsInterface(new ContactHelper())
                .build();
    }

    public Model findSuggestion(String suggestion) {
        return recognizer.parse(suggestion);
    }

    public Reminder findResults(ArrayList matches) {
        for (int i = 0; i < matches.size(); i++) {
            Object key = matches.get(i);
            String keyStr = key.toString();
            Model model = recognizer.parse(keyStr);
            if (model != null) {
                LogUtil.d(TAG, "parseResults: " + model);
                return createReminder(model);
            }
        }
        return null;
    }

    public void parseResults(ArrayList matches, boolean isWidget) {
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
                    } else if (action == Action.TRASH) {
                        emptyTrash(true, null);
                    } else if (action == Action.DISABLE) {
                        disableAllReminders(true);
                    } else if (action == Action.SETTINGS) {
                        Intent startActivityIntent = new Intent(mContext, MainActivity.class);
                        startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_settings);
                        mContext.startActivity(startActivityIntent);
                    } else if (action == Action.REPORT) {
                        Intent startActivityIntent = new Intent(mContext, MainActivity.class);
                        startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_feedback);
                        mContext.startActivity(startActivityIntent);
                    }
                } else if (types == ActionType.NOTE) {
                    saveNote(createNote(model.getSummary()), true, true);
                } else if (types == ActionType.REMINDER) {
                    saveReminder(model, isWidget);
                } else if (types == ActionType.GROUP) {
                    saveGroup(createGroup(model), true);
                }
                break;
            }
        }
    }

    @NonNull
    public GroupItem createGroup(Model model) {
        return new GroupItem(model.getSummary(), new Random().nextInt(16));
    }


    public void saveGroup(GroupItem model, boolean showToast) {
        RealmDb.getInstance().saveObject(model);
        if (showToast) {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    public void disableAllReminders(boolean showToast) {
        for (Reminder reminder : RealmDb.getInstance().getEnabledReminders()) {
            EventControl control = EventControlFactory.getController(mContext, reminder);
            control.stop();
        }
        if (showToast) {
            Toast.makeText(mContext, R.string.all_reminders_were_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteReminder(Reminder reminder) {
        EventControl control = EventControlFactory.getController(mContext, reminder);
        control.stop();
        RealmDb.getInstance().deleteReminder(reminder.getUuId());
        CalendarUtils.deleteEvents(mContext, reminder.getUuId());
    }

    public void emptyTrash(boolean showToast, ThreadCallback callback) {
        DataLoader.loadArchivedReminder(result -> {
            for (Reminder reminder : result) {
                deleteReminder(reminder);
            }
            if (showToast) {
                Toast.makeText(mContext, R.string.trash_cleared, Toast.LENGTH_SHORT).show();
            }
            if (callback != null) {
                callback.onDone();
            }
        });
    }

    private void saveReminder(Model model, boolean widget) {
        Reminder reminder = createReminder(model);
        EventControl control = EventControlFactory.getController(mContext, reminder);
        control.start();
        if (widget) {
            mContext.startActivity(new Intent(mContext, VoiceResultDialog.class)
                    .putExtra(Constants.INTENT_ID, reminder.getUuId())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    public Reminder createReminder(Model model) {
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
        return reminder;
    }

    @NonNull
    public NoteItem createNote(String note) {
        int color = new Random().nextInt(15);
        NoteItem item = new NoteItem();
        item.setColor(color);
        item.setSummary(note);
        item.setDate(TimeUtil.getGmtDateTime());
        return item;
    }

    public void saveNote(NoteItem note, boolean showToast, boolean addQuickNote) {
        Prefs prefs = Prefs.getInstance(mContext);
        if (addQuickNote && prefs.getBoolean(Prefs.QUICK_NOTE_REMINDER)) {
            saveQuickReminder(note.getKey(), note.getSummary());
        }
        RealmDb.getInstance().saveObject(note);
        UpdatesHelper.getInstance(mContext).updateNotesWidget();
        if (showToast) {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    public Reminder saveQuickReminder(String key, String summary) {
        long after = Prefs.getInstance(mContext).getInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
        long due = System.currentTimeMillis() + after;
        Reminder mReminder = new Reminder();
        mReminder.setType(Reminder.BY_DATE);
        mReminder.setDelay(0);
        mReminder.setEventCount(0);
        mReminder.setUseGlobal(true);
        mReminder.setNoteId(key);
        mReminder.setSummary(summary);
        mReminder.setGroupUuId(RealmDb.getInstance().getDefaultGroup().getUuId());
        mReminder.setStartTime(TimeUtil.getGmtFromDateTime(due));
        mReminder.setEventTime(TimeUtil.getGmtFromDateTime(due));
        RealmDb.getInstance().saveObject(mReminder);
        EventControl control = EventControlFactory.getController(mContext, mReminder);
        control.start();
        return mReminder;
    }

    private static class ContactHelper implements ContactsInterface {

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

    public interface ThreadCallback {
        void onDone();
    }
}