package com.elementary.tasks.notes;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.ReminderApp;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.BitmapUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityCreateNoteBinding;
import com.elementary.tasks.databinding.DialogColorPickerLayoutBinding;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

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

public class ActivityCreateNote extends ThemedActivity {
    public static final int MENU_ITEM_DELETE = 12;
    private static final int REQUEST_SD_CARD = 1112;

    private int mHour = 0;
    private int mMinute = 0;
    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 1;
    private int mColor = 0;
    private int mFontStyle = 0;
    private Bitmap img;
    private Uri mImageUri;
    private byte[] mImage;

    private RelativeLayout layoutContainer, imageContainer;
    private LinearLayout remindContainer;
    private RoboTextView remindDate, remindTime;
    private ImageButton discardReminder;
    private ImageView noteImage;
    private ActivityCreateNoteBinding binding;

    private NoteItem mItem;
    private Toolbar toolbar;
    private EditText taskField;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        taskField = binding.taskMessage;
        taskField.setTextSize(Prefs.getInstance(this).getNoteTextSize() + 12);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0f);

        toolbar.setVisibility(View.VISIBLE);

        layoutContainer = binding.layoutContainer;
        imageContainer = binding.imageContainer;
        remindContainer = binding.remindContainer;
        ViewUtils.fadeInAnimation(layoutContainer);
        remindDate = binding.remindDate;
        remindDate.setOnClickListener(v -> dateDialog().show());
        remindTime = binding.remindTime;
        remindTime.setOnClickListener(v -> timeDialog().show());
        noteImage = binding.noteImage;
        noteImage.setOnClickListener(v -> openImage());
        discardReminder = binding.discardReminder;
        discardReminder.setOnClickListener(v -> ViewUtils.collapse(remindContainer));
        binding.deleteButton.setOnClickListener(v -> {
            if (isImageAttached()) {
                ViewUtils.collapse(imageContainer);
                img = null;
                mImage = null;
                mItem.setImage(null);
            }
        });
        setImages();
        Intent intent = getIntent();
        String filePath = intent.getStringExtra(Constants.FILE_PICKED);
        Uri name = null;
        try {
            name = intent.getData();
        } catch (NullPointerException e){
            e.printStackTrace();
        } finally {
            String id = intent.getStringExtra(Constants.INTENT_ID);
            if (id != null) {
                mItem = RealmDb.getInstance().getNote(id);
            }
        }
        if (name != null){
            String scheme = name.getScheme();
            if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                ContentResolver cr = getApplicationContext().getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(name);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedReader r = null;
                if (is != null) {
                    r = new BufferedReader(new InputStreamReader(is));
                }
                StringBuilder total = new StringBuilder();
                String line;
                try {
                    while ((line = r != null ? r.readLine() : null) != null) {
                        total.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String file = total.toString();
//                try {
//                    mItem = SyncHelper.getNote(null, file);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            } else {
//                try {
//                    mItem = SyncHelper.getNote(name.getPath(), null);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        } else if (filePath != null) {
//            try {
//                mItem = SyncHelper.getNote(filePath, null);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }

        if (mItem != null){
            String note = mItem.getSummary();
            mColor = mItem.getColor();
            mFontStyle = mItem.getStyle();
            taskField.setText(note);
            taskField.setSelection(taskField.getText().length());
            mImage = mItem.getImage();
            if (mImage != null){
                img = BitmapFactory.decodeByteArray(mImage, 0, mImage.length);
                noteImage.setImageBitmap(img);
                ViewUtils.expand(imageContainer);
            } else {
                imageContainer.setVisibility(View.GONE);
            }
            showReminder();
        } else {
            mColor = new Random().nextInt(16);
        }
        updateBackground();
        updateTextStyle();
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            ReminderApp application = (ReminderApp) getApplication();
            mTracker = application.getDefaultTracker();
        }
    }

    private void showReminder() {
//        ReminderItem item = ReminderHelper.getInstance(this).getReminder(mItem.getLinkId());
//        if (item != null) {
//            setDateTime(item.getDateTime());
//            ViewUtils.expand(remindContainer);
//        }
    }

    private void openImage() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Bitmap _bitmapScaled = img;
            _bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File path = MemoryUtil.getImageCacheDir();
            boolean isDirectory = true;
            if (!path.exists()) {
                isDirectory = path.mkdirs();
            }
            if (isDirectory) {
                String fileName = UUID.randomUUID().toString() + FileConfig.FILE_NAME_IMAGE;
                File f = new File(path + File.separator + fileName);
                boolean isCreated = f.createNewFile();
                if (isCreated) {
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                    startActivity(new Intent(this, ImagePreviewActivity.class)
                            .putExtra(Constants.FILE_PICKED, f.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void shareNote() {
//        SyncHelper sHelp = new SyncHelper(NotesActivity.this);
//        String note = taskField.getText().toString();
//        if (note.matches("")) {
//            taskField.setError(getString(R.string.must_be_not_empty));
//            return;
//        }
//        Calendar calendar1 = Calendar.getInstance();
//        int day = calendar1.get(Calendar.DAY_OF_MONTH);
//        int month = calendar1.get(Calendar.MONTH);
//        int year = calendar1.get(Calendar.YEAR);
//        String date = year + "/" + month + "/" + day;
//        String uuID = mItem.getUuId();
//        if (uuID == null || uuID.matches("")) {
//            uuID = SyncHelper.generateID();
//        }
//        mItem.setNote(note);
//        mItem.setDate(date);
//        mItem.setUuId(uuID);
//        try {
//            File file = sHelp.createNote(mItem);
//            sendMail(file, note);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    private void sendMail(File file, String text){
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
//        Telephony.sendNote(file, this, text);
    }

    private void setDateTime(long mills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mills);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mMonth = calendar.get(Calendar.MONTH);
        mYear = calendar.get(Calendar.YEAR);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        remindDate.setText(TimeUtil.getDate(calendar.getTimeInMillis()));
        remindTime.setText(TimeUtil.getTime(calendar.getTime(), Prefs.getInstance(this).is24HourFormatEnabled()));
    }

    private boolean isReminderAttached(){
        return remindContainer.getVisibility() == View.VISIBLE;
    }

    private boolean isImageAttached(){
        return imageContainer.getVisibility() == View.VISIBLE;
    }

    private void saveNote() {
        String note = taskField.getText().toString().trim();
        if (note.matches("") && mItem.getImage() == null) {
            taskField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        if (mItem == null) {
            mItem = new NoteItem(UUID.randomUUID().toString());
        }
        mItem.setSummary(note);
        mItem.setDate(TimeUtil.getTimeStamp());
        mItem.setImage(mImage);
        mItem.setColor(mColor);
        mItem.setStyle(mFontStyle);
        boolean hasReminder = isReminderAttached();
        if (!hasReminder) removeNoteFromReminder(mItem.getKey());
        RealmDb.getInstance().saveObject(mItem);
        if (hasReminder){
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth, mDay, mHour, mMinute);
            createReminder(mItem.getKey(), calendar);
        }
        finish();
    }

    private void createReminder(String key, Calendar calendar) {

    }

    private void removeNoteFromReminder(String key) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_color:
                showColorDialog();
                return true;
            case R.id.action_image:
                if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                    getImage();
                } else {
                    Permissions.requestPermission(this, REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
                }
                return true;
            case R.id.action_reminder:
                if (!isReminderAttached()) {
                    setDateTime(System.currentTimeMillis());
                    ViewUtils.expand(remindContainer);
                } else {
                    ViewUtils.collapse(remindContainer);
                }
                return true;
            case R.id.action_font:
                showStyleDialog();
                return true;
            case R.id.action_share:
                shareNote();
                return true;
            case MENU_ITEM_DELETE:
                deleteDialog();
                return true;
            case R.id.action_add:
                saveNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showColorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_color));
        DialogColorPickerLayoutBinding binding = DialogColorPickerLayoutBinding.inflate(LayoutInflater.from(this));
        ColorPickerView view = binding.pickerView;
        view.setSelectedColor(mColor);
        builder.setView(binding.getRoot());
        AlertDialog dialog = builder.create();
        view.setListener(code -> {
            mColor = code;
            updateBackground();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            RealmDb.getInstance().deleteNote(mItem);
            finish();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setImages(){
        if (themeUtil.isDark()){
            discardReminder.setImageResource(R.drawable.ic_clear_white_24dp);
        } else {
            discardReminder.setImageResource(R.drawable.ic_clear_black_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_note, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    private void getImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.image));
        builder.setItems(new CharSequence[]{getString(R.string.gallery),
                        getString(R.string.take_a_shot)},
                (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            Intent chooser = Intent.createChooser(intent, getString(R.string.image));
                            startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                        }
                        break;
                        case 1: {
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "Picture");
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                            mImageUri = getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            startActivityForResult(intent, Constants.ACTION_REQUEST_CAMERA);
                        }
                        break;
                        default:
                            break;
                    }
                });

        builder.show();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_REQUEST_GALLERY:
                    Uri selectedImage = data.getData();
                    getImageFromGallery(selectedImage);
                    break;
                case Constants.ACTION_REQUEST_CAMERA:
                    getImageFromCamera();
                    break;
            }
        }
    }

    private void getImageFromGallery(Uri selectedImage) {
        Bitmap bitmapImage = null;
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(this, selectedImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        img = bitmapImage;
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            mImage = outputStream.toByteArray();
            noteImage.setImageBitmap(bitmapImage);
            if (!isImageAttached()) {
                ViewUtils.expand(imageContainer);
            }
        }
    }

    private void getImageFromCamera() {
        Bitmap bitmapImage = null;
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(this, mImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        img = bitmapImage;
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            mImage = outputStream.toByteArray();
            noteImage.setImageBitmap(bitmapImage);
            if (!isImageAttached()) {
                ViewUtils.expand(imageContainer);
            }
            String pathFromURI = getRealPathFromURI(mImageUri);
            File file = new File(pathFromURI);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void updateTextStyle() {
        taskField.setTypeface(AssetsUtil.getTypeface(this, mFontStyle));
    }

    private void updateBackground() {
        layoutContainer.setBackgroundColor(themeUtil.getNoteLightColor(mColor));
        toolbar.setBackgroundColor(themeUtil.getNoteLightColor(mColor));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(themeUtil.getNoteDarkColor(mColor));
        }
    }

    private void showStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.font_style));
        ArrayList<String> contacts = new ArrayList<>();
        contacts.clear();
        contacts.add("Black");
        contacts.add("Black Italic");
        contacts.add("Bold");
        contacts.add("Bold Italic");
        contacts.add("Italic");
        contacts.add("Light");
        contacts.add("Light Italic");
        contacts.add("Medium");
        contacts.add("Medium Italic");
        contacts.add("Regular");
        contacts.add("Thin");
        contacts.add("Thin Italic");
        final LayoutInflater inflater = LayoutInflater.from(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, contacts) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
                }
                TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                textView.setTypeface(getTypeface(position));
                textView.setText(contacts.get(position));
                return convertView;
            }

            private Typeface getTypeface(int position){
                return AssetsUtil.getTypeface(ActivityCreateNote.this, position);
            }
        };
        builder.setSingleChoiceItems(adapter, mFontStyle, (dialog, which) -> {
            mFontStyle = which;
            updateTextStyle();
        });
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected Dialog dateDialog() {
        return new DatePickerDialog(this, myDateCallBack, mYear, mMonth, mDay);
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String dayStr;
            String monthStr;
            if (mDay < 10) {
                dayStr = "0" + mDay;
            } else {
                dayStr = String.valueOf(mDay);
            }
            if (mMonth < 9) {
                monthStr = "0" + (mMonth + 1);
            } else {
                monthStr = String.valueOf(mMonth + 1);
            }
            remindDate.setText(SuperUtil.appendString(dayStr, "/", monthStr, "/", String.valueOf(mYear)));
        }
    };

    protected Dialog timeDialog() {
        return new TimePickerDialog(this, myCallBack, mHour, mMinute,
                Prefs.getInstance(this).is24HourFormatEnabled());
    }

    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            remindTime.setText(TimeUtil.getTime(c.getTime(),
                    Prefs.getInstance(ActivityCreateNote.this).is24HourFormatEnabled()));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Create note screen");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImage();
                }
                break;
        }
    }
}