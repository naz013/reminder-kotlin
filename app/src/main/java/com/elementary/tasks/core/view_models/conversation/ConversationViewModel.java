package com.elementary.tasks.core.view_models.conversation;

import android.app.Application;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.Toast;

import com.backdoor.engine.Action;
import com.backdoor.engine.ActionType;
import com.backdoor.engine.ContactOutput;
import com.backdoor.engine.ContactsInterface;
import com.backdoor.engine.Model;
import com.backdoor.engine.Recognizer;
import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.AddBirthdayActivity;
import com.elementary.tasks.core.SplashScreen;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.dialogs.VoiceHelpDialog;
import com.elementary.tasks.core.dialogs.VoiceResultDialog;
import com.elementary.tasks.core.dialogs.VolumeDialog;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.Commands;
import com.elementary.tasks.core.view_models.reminders.BaseRemindersViewModel;
import com.elementary.tasks.navigation.MainActivity;
import com.elementary.tasks.reminder.create_edit.AddReminderActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Copyright 2018 Nazar Suhovich
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
public class ConversationViewModel extends BaseRemindersViewModel {

    private static final String TAG = "ConversationViewModel";

    private Recognizer recognizer;

    public ConversationViewModel(Application application) {
        super(application);

        Prefs prefs = Prefs.getInstance(application);
        String language = Language.getLanguage(prefs.getVoiceLocale());
        String morning = prefs.getMorningTime();
        String day = prefs.getNoonTime();
        String evening = prefs.getEveningTime();
        String night = prefs.getNightTime();
        String[] times = new String[]{morning, day, evening, night};
        recognizer = new Recognizer.Builder()
                .setLocale(language)
                .setTimes(times)
                .setContactsInterface(new ContactHelper())
                .build();
    }

    @Nullable
    public Model findSuggestion(String suggestion) {
        return recognizer.parse(suggestion);
    }

    @Nullable
    public Reminder findResults(@NonNull ArrayList matches) {
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

    public void parseResults(@NonNull ArrayList matches, boolean isWidget) {
        for (int i = 0; i < matches.size(); i++) {
            Object key = matches.get(i);
            String keyStr = key.toString();
            Model model = findSuggestion(keyStr);
            if (model != null) {
                LogUtil.d(TAG, "parseResults: " + model);
                ActionType types = model.getType();
                if (types == ActionType.ACTION && isWidget) {
                    Action action = model.getAction();
                    if (action == Action.APP) {
                        getApplication().startActivity(new Intent(getApplication(), SplashScreen.class));
                    } else if (action == Action.HELP) {
                        getApplication().startActivity(new Intent(getApplication(), VoiceHelpDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    } else if (action == Action.BIRTHDAY) {
                        getApplication().startActivity(new Intent(getApplication(), AddBirthdayActivity.class));
                    } else if (action == Action.REMINDER) {
                        getApplication().startActivity(new Intent(getApplication(), AddReminderActivity.class));
                    } else if (action == Action.VOLUME) {
                        getApplication().startActivity(new Intent(getApplication(), VolumeDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    } else if (action == Action.TRASH) {
                        emptyTrash(true);
                    } else if (action == Action.DISABLE) {
                        disableAllReminders(true);
                    } else if (action == Action.SETTINGS) {
                        Intent startActivityIntent = new Intent(getApplication(), MainActivity.class);
                        startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_settings);
                        getApplication().startActivity(startActivityIntent);
                    } else if (action == Action.REPORT) {
                        Intent startActivityIntent = new Intent(getApplication(), MainActivity.class);
                        startActivityIntent.putExtra(Constants.INTENT_POSITION, R.id.nav_feedback);
                        getApplication().startActivity(startActivityIntent);
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

    private void saveReminder(@NonNull Model model, boolean widget) {
        Reminder reminder = createReminder(model);
        saveAndStartReminder(reminder);
        if (widget) {
            getApplication().startActivity(new Intent(getApplication(), VoiceResultDialog.class)
                    .putExtra(Constants.INTENT_ID, reminder.getUuId())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show();
        }
    }

    public void disableAllReminders(boolean showToast) {
        isInProgress.postValue(true);
        run(() -> {
            for (Reminder reminder : getAppDb().reminderDao().getAll(true, false)) {
                stopReminder(reminder);
            }
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.DELETED);
                if (showToast) {
                    Toast.makeText(getApplication(), R.string.all_reminders_were_disabled, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void emptyTrash(boolean showToast) {
        isInProgress.postValue(true);
        run(() -> {
            List<Reminder> archived = getAppDb().reminderDao().getAll(false, true);
            for (Reminder reminder : archived) {
                deleteReminder(reminder, false);
                CalendarUtils.deleteEvents(getApplication(), reminder.getUniqueId());
            }
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.TRASH_CLEARED);
                if (showToast) {
                    Toast.makeText(getApplication(), R.string.trash_cleared, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @NonNull
    public Reminder createReminder(@NonNull Model model) {
        Action action = model.getAction();
        String number = model.getTarget();
        String summary = model.getSummary();
        long repeat = model.getRepeatInterval();
        List<Integer> weekdays = model.getWeekdays();
        boolean isCalendar = model.isHasCalendar();
        String startTime = model.getDateTime();
        long eventTime = TimeUtil.getDateTimeFromGmt(startTime);
        int typeT = Reminder.BY_DATE;
        if (action == Action.WEEK || action == Action.WEEK_CALL || action == Action.WEEK_SMS) {
            typeT = Reminder.BY_WEEK;
            eventTime = TimeCount.getInstance(getApplication()).getNextWeekdayTime(TimeUtil.getDateTimeFromGmt(startTime), weekdays, 0);
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
        Group item = defaultGroup.getValue();
        String categoryId = "";
        if (item != null) {
            categoryId = item.getUuId();
        }
        Prefs prefs = Prefs.getInstance(getApplication());
        boolean isCal = prefs.getBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = prefs.getBoolean(Prefs.EXPORT_TO_STOCK);
        Reminder reminder = new Reminder();
        reminder.setType(typeT);
        reminder.setSummary(summary);
        reminder.setGroupUuId(categoryId);
        reminder.setWeekdays(weekdays);
        reminder.setRepeatInterval(repeat);
        reminder.setTarget(number);
        reminder.setEventTime(TimeUtil.getGmtFromDateTime(eventTime));
        reminder.setStartTime(TimeUtil.getGmtFromDateTime(eventTime));
        reminder.setExportToCalendar(isCalendar && (isCal || isStock));
        return reminder;
    }

    @NonNull
    public Note createNote(@Nullable String note) {
        int color = new Random().nextInt(15);
        Note item = new Note();
        item.setColor(color);
        item.setSummary(note);
        item.setDate(TimeUtil.getGmtDateTime());
        return item;
    }

    public void saveNote(@NonNull Note note, boolean showToast, boolean addQuickNote) {
        Prefs prefs = Prefs.getInstance(getApplication());
        if (addQuickNote && prefs.getBoolean(Prefs.QUICK_NOTE_REMINDER)) {
            saveQuickReminder(note.getKey(), note.getSummary());
        }
        getAppDb().notesDao().insert(note);
        UpdatesHelper.getInstance(getApplication()).updateNotesWidget();
        if (showToast) {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    public Reminder saveQuickReminder(@Nullable String key, @Nullable String summary) {
        long after = Prefs.getInstance(getApplication()).getInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
        long due = System.currentTimeMillis() + after;
        Reminder mReminder = new Reminder();
        mReminder.setType(Reminder.BY_DATE);
        mReminder.setDelay(0);
        mReminder.setEventCount(0);
        mReminder.setUseGlobal(true);
        mReminder.setNoteId(key);
        mReminder.setSummary(summary);
        Group def = defaultGroup.getValue();
        if (def != null) {
            mReminder.setGroupUuId(def.getUuId());
        }
        mReminder.setStartTime(TimeUtil.getGmtFromDateTime(due));
        mReminder.setEventTime(TimeUtil.getGmtFromDateTime(due));
        saveAndStartReminder(mReminder);
        return mReminder;
    }

    @NonNull
    public Group createGroup(@NonNull Model model) {
        return new Group(model.getSummary(), new Random().nextInt(16));
    }

    public void saveGroup(@NonNull Group model, boolean showToast) {
        getAppDb().groupDao().insert(model);
        if (showToast) {
            Toast.makeText(getApplication(), R.string.saved, Toast.LENGTH_SHORT).show();
        }
    }

    private class ContactHelper implements ContactsInterface {

        @Override
        public ContactOutput findEmail(String input) {
            if (!Permissions.checkPermission(getApplication(), Permissions.READ_CONTACTS)) {
                return null;
            }
            String number = null;
            String[] parts = input.split("\\s");
            for (String part : parts) {
                while (part.length() > 1) {
                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Email.DATA};
                    Cursor c = getApplication().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
        public ContactOutput findNumber(String input) {
            if (!Permissions.checkPermission(getApplication(), Permissions.READ_CONTACTS)) {
                return null;
            }
            String number = null;
            String[] parts = input.split("\\s");
            for (String part : parts) {
                while (part.length() > 1) {
                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + part + "%'";
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor c = getApplication().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
