package com.elementary.tasks.notes.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.drawing.DrawView;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.BitmapUtils;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.IconRadioButton;
import com.elementary.tasks.core.views.ThemedImageButton;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.databinding.DrawFragmentBinding;
import com.elementary.tasks.databinding.ImagePrefsBinding;
import com.elementary.tasks.databinding.LayersPrefsBinding;
import com.elementary.tasks.databinding.StandardPrefsBinding;
import com.elementary.tasks.databinding.TextPrefsBinding;
import com.elementary.tasks.notes.NoteImage;
import com.elementary.tasks.notes.editor.layers.LayersRecyclerAdapter;
import com.elementary.tasks.notes.editor.layers.SimpleItemTouchHelperCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

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

public class DrawFragment extends BitmapFragment {

    private static final String TAG = "DrawFragment";
    private static final int REQUEST_SD_CARD = 1112;

    private DrawFragmentBinding binding;
    private DrawView mView;
    private ThemedImageButton mControlButton;

    private ItemTouchHelper mItemTouchHelper;
    private LayersRecyclerAdapter mAdapter;

    private Uri mImageUri;

    @ColorRes
    private int strokeColor;
    @ColorRes
    private int fillColor;
    private boolean isFillPicker;

    private DrawView.DrawCallback mDrawCallback = () -> {
        checkRedo();
        checkUndo();
    };

    public static DrawFragment newInstance() {
        return new DrawFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DrawFragmentBinding.inflate(inflater, container, false);
        setUiTheme();
        mView = binding.drawView;
        mView.setCallback(mDrawCallback);
        initDrawControl();
        initControls();
        initColorControls();
        loadImage();
        new Handler().postDelayed(this::hideToolPanel, 1000);
        new Handler().postDelayed(this::hideColorPanel, 1000);
        return binding.getRoot();
    }

    private void initColorControls() {
        setColor(R.color.blackPrimary);
        setCurrentColor(binding.colorBlackButton);
        isFillPicker = true;
        setColor(R.color.whitePrimary);
        setCurrentColor(binding.colorWhiteButton);
        binding.colorGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            hideColorPanel();
            switch (i) {
                case R.id.colorAmberButton:
                    setColor(R.color.amberPrimary);
                    setCurrentColor(binding.colorAmberButton);
                    break;
                case R.id.colorBlackButton:
                    setColor(R.color.blackPrimary);
                    setCurrentColor(binding.colorBlackButton);
                    break;
                case R.id.colorBlueButton:
                    setColor(R.color.bluePrimary);
                    setCurrentColor(binding.colorBlueButton);
                    break;
                case R.id.colorBlueLightButton:
                    setColor(R.color.blueLightPrimary);
                    setCurrentColor(binding.colorBlueLightButton);
                    break;
                case R.id.colorCyanButton:
                    setColor(R.color.cyanPrimary);
                    setCurrentColor(binding.colorCyanButton);
                    break;
                case R.id.colorDeepOrangeButton:
                    setColor(R.color.orangeDeepPrimary);
                    setCurrentColor(binding.colorDeepOrangeButton);
                    break;
                case R.id.colorDeepPurpleButton:
                    setColor(R.color.purpleDeepPrimary);
                    setCurrentColor(binding.colorDeepPurpleButton);
                    break;
                case R.id.colorGreenButton:
                    setColor(R.color.greenPrimary);
                    setCurrentColor(binding.colorGreenButton);
                    break;
                case R.id.colorGreenLightButton:
                    setColor(R.color.greenLightPrimary);
                    setCurrentColor(binding.colorGreenLightButton);
                    break;
                case R.id.colorGreyButton:
                    setColor(R.color.material_divider);
                    setCurrentColor(binding.colorGreyButton);
                    break;
                case R.id.colorIndigoButton:
                    setColor(R.color.indigoPrimary);
                    setCurrentColor(binding.colorIndigoButton);
                    break;
                case R.id.colorLimeButton:
                    setColor(R.color.limePrimary);
                    setCurrentColor(binding.colorLimeButton);
                    break;
                case R.id.colorOrangeButton:
                    setColor(R.color.orangePrimary);
                    setCurrentColor(binding.colorOrangeButton);
                    break;
                case R.id.colorPinkButton:
                    setColor(R.color.pinkPrimary);
                    setCurrentColor(binding.colorPinkButton);
                    break;
                case R.id.colorPurpleButton:
                    setColor(R.color.purplePrimary);
                    setCurrentColor(binding.colorPurpleButton);
                    break;
                case R.id.colorRedButton:
                    setColor(R.color.redPrimary);
                    setCurrentColor(binding.colorRedButton);
                    break;
                case R.id.colorTealButton:
                    setColor(R.color.tealPrimary);
                    setCurrentColor(binding.colorTealButton);
                    break;
                case R.id.colorWhiteButton:
                    setColor(R.color.whitePrimary);
                    setCurrentColor(binding.colorWhiteButton);
                    break;
                case R.id.colorYellowButton:
                    setColor(R.color.yellowPrimary);
                    setCurrentColor(binding.colorYellowButton);
                    break;
            }
        });
    }

    private void setColor(@ColorRes int color) {
        if (color == 0) return;
        if (isFillPicker) {
            fillColor = color;
            mView.setPaintFillColor(getResources().getColor(color));
        } else {
            strokeColor = color;
            mView.setPaintStrokeColor(getResources().getColor(color));
        }
    }

    private void setUiTheme() {
        int bgColor = ThemeUtil.getInstance(getContext()).getBackgroundStyle();
        binding.background.setBackgroundColor(bgColor);
        binding.drawTools.setBackgroundColor(bgColor);
        binding.colorView.setBackgroundColor(bgColor);
        binding.controlBox.setBackgroundColor(bgColor);
        binding.prefsView.setBackgroundColor(bgColor);
    }

    private void initControls() {
        binding.undoButton.setOnClickListener(view -> undo());
        binding.redoButton.setOnClickListener(view -> redo());
        binding.clearButton.setOnClickListener(view -> mView.clear());
        binding.fillButton.setOnClickListener(view -> setBackground());
        binding.currentToolButton.setOnClickListener(view -> toggleToolPanel());
        binding.currentFillColorButton.setOnClickListener(view -> {
            isFillPicker = true;
            toggleColorPanel();
        });
        binding.currentStrokeColorButton.setOnClickListener(view -> {
            isFillPicker = false;
            toggleColorPanel();
        });
        binding.showPrefsButton.setOnClickListener(v -> switchPrefsPanel(mView.getMode()));
        binding.layersButton.setOnClickListener(v -> showLayersPanel());
    }

    private void showLayersPanel() {
        if (isPrefsPanelExpanded()) {
            hidePrefsPanel();
        } else {
            switchPrefsPanel(DrawView.Mode.LAYERS);
        }
    }

    private void togglePrefsPanel() {
        if (isPrefsPanelExpanded()) {
            hidePrefsPanel();
        } else {
            showPrefsPanel();
        }
    }

    private boolean isPrefsPanelExpanded() {
        return binding.prefsView.getVisibility() == View.VISIBLE;
    }

    private void toggleColorPanel() {
        if (binding.colorView.getVisibility() == View.VISIBLE) {
            hideColorPanel();
        } else {
            showColorPanel();
        }
    }

    private void showColorPanel() {
        if (isFillPicker) {
            binding.colorGroup.check(getId(fillColor));
        } else {
            binding.colorGroup.check(getId(strokeColor));
        }
        ViewUtils.slideInDown(getContext(), binding.colorView);
    }

    private void hideColorPanel() {
        ViewUtils.slideOutUp(getContext(), binding.colorView);
    }

    private void toggleToolPanel() {
        if (binding.drawTools.getVisibility() == View.VISIBLE) {
            hideToolPanel();
        } else {
            showToolPanel();
        }
    }

    private void showToolPanel() {
        ViewUtils.slideInDown(getContext(), binding.drawTools);
    }

    private void hideToolPanel() {
        ViewUtils.slideOutUp(getContext(), binding.drawTools);
    }

    private void setBackground() {
        mView.setBaseColor(mView.getPaintFillColor());
    }

    private void redo() {
        if (mView.canRedo()) {
            mView.redo();
        }
        checkRedo();
    }

    private void checkRedo() {
        if (mView.canRedo()) {
            binding.redoButton.setEnabled(true);
        } else {
            binding.redoButton.setEnabled(false);
        }
    }

    private void undo() {
        if (mView.canUndo()) {
            mView.undo();
        }
        checkUndo();
    }

    private void checkUndo() {
        if (mView.canUndo()) {
            binding.undoButton.setEnabled(true);
        } else {
            binding.undoButton.setEnabled(false);
        }
    }

    private void initDrawControl() {
        binding.toolsGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            hideToolPanel();
            switch (i) {
                case R.id.penButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.PEN);
                    setCurrentTool(binding.penButton);
                    break;
                case R.id.lineButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.LINE);
                    setCurrentTool(binding.lineButton);
                    break;
                case R.id.cubicBezierButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.QUADRATIC_BEZIER);
                    setCurrentTool(binding.cubicBezierButton);
                    break;
                case R.id.textButton:
                    setDrawMode(DrawView.Mode.TEXT, null);
                    setCurrentTool(binding.textButton);
                    break;
                case R.id.rectangleButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.RECTANGLE);
                    setCurrentTool(binding.rectangleButton);
                    break;
                case R.id.ellipseButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.ELLIPSE);
                    setCurrentTool(binding.ellipseButton);
                    break;
                case R.id.circleButton:
                    setDrawMode(DrawView.Mode.DRAW, DrawView.Drawer.CIRCLE);
                    setCurrentTool(binding.circleButton);
                    break;
                case R.id.imageButton:
                    setDrawMode(DrawView.Mode.IMAGE, null);
                    setCurrentTool(binding.imageButton);
                    break;
            }
        });
        binding.currentFillColorButton.setIcon(binding.colorWhiteButton.getIcon());
        binding.currentStrokeColorButton.setIcon(binding.colorBlackButton.getIcon());
        switchPrefsPanel(mView.getMode());
    }

    private void switchPrefsPanel(DrawView.Mode mode) {
        if (isPrefsPanelExpanded()) {
            hidePrefsPanel();
        }
        binding.prefsView.removeAllViewsInLayout();
        if (mode == DrawView.Mode.DRAW) {
            binding.prefsView.addView(getPenPanel().getRoot());
        } else if (mode == DrawView.Mode.TEXT) {
            binding.prefsView.addView(getTextPanel().getRoot());
        } else if (mode == DrawView.Mode.IMAGE) {
            binding.prefsView.addView(getImagePanel().getRoot());
        } else if (mode == DrawView.Mode.LAYERS) {
            binding.prefsView.addView(getLayersPanel().getRoot());
        }
        showPrefsPanel();
    }

    private LayersPrefsBinding getLayersPanel() {
        if (mAdapter != null) {
            mView.removeObserver(mAdapter);
        }
        LayersPrefsBinding binding = LayersPrefsBinding.inflate(LayoutInflater.from(getContext()));
        binding.layersList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new LayersRecyclerAdapter(getContext(), mView.getElements(),
                viewHolder -> mItemTouchHelper.startDrag(viewHolder),
                new LayersRecyclerAdapter.AdapterCallback() {
                    @Override
                    public void onChanged() {
                        mView.invalidate();
                    }

                    @Override
                    public void onItemSelect(int position) {
                        mAdapter.setIndex(position);
                        mView.setHistoryPointer(position + 1);
                    }

                    @Override
                    public void onItemRemoved(int position) {
                        mView.setHistoryPointer(mView.getHistoryPointer() - 1);
                    }

                    @Override
                    public void onItemAdded() {
                        binding.layersList.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                });
        mAdapter.setIndex(mView.getHistoryPointer() - 1);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(binding.layersList);
        binding.layersList.setAdapter(mAdapter);
        mControlButton = binding.prefsControl;
        binding.prefsControl.setOnClickListener(v -> togglePrefsPanel());
        mView.addObserver(mAdapter);
        return binding;
    }

    private ImagePrefsBinding getImagePanel() {
        ImagePrefsBinding binding = ImagePrefsBinding.inflate(LayoutInflater.from(getContext()));
        binding.opacitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mView.setOpacity(i, DrawView.Mode.IMAGE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.opacitySeek.setProgress(mView.getOpacity());
        binding.scaleSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mView.setScale(progress, DrawView.Mode.IMAGE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.scaleSeek.setProgress(mView.getScale());
        mControlButton = binding.prefsControl;
        binding.prefsControl.setOnClickListener(v -> togglePrefsPanel());
        binding.imageButton.setOnClickListener(v -> showImagePickerDialog());
        return binding;
    }

    private void showImagePickerDialog() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(getActivity(), REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.MANAGE_DOCUMENTS);
            return;
        }
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        builder.setTitle(getString(R.string.image));
        builder.setItems(new CharSequence[]{getString(R.string.gallery),
                        getString(R.string.take_a_shot)},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            pickFromGallery();
                            break;
                        case 1:
                            pickFromCamera();
                            break;
                    }
                });

        builder.create().show();
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        mImageUri = getContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, Constants.ACTION_REQUEST_CAMERA);
    }

    private void pickFromGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, Constants.ACTION_REQUEST_GALLERY);
    }

    private TextPrefsBinding getTextPanel() {
        TextPrefsBinding binding = TextPrefsBinding.inflate(LayoutInflater.from(getContext()));
        binding.opacitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mView.setOpacity(i, DrawView.Mode.TEXT);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.opacitySeek.setProgress(mView.getOpacity());
        binding.widthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mView.setFontSize(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.widthSeek.setProgress((int) mView.getFontSize());
        binding.addButton.setOnClickListener(view -> showTextPickerDialog());
        mControlButton = binding.prefsControl;
        binding.prefsControl.setOnClickListener(v -> togglePrefsPanel());
        binding.fontButton.setOnClickListener(v -> showStyleDialog());
        return binding;
    }

    private void showStyleDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
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
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_single_choice, contacts) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setTypeface(getTypeface(position));
                textView.setText(contacts.get(position));
                return convertView;
            }

            private Typeface getTypeface(int position) {
                return AssetsUtil.getTypeface(getContext(), position);
            }
        };
        builder.setSingleChoiceItems(adapter, mView.getFontFamily(), (dialog, which) -> {
            mView.setFontFamily(which);
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private StandardPrefsBinding getPenPanel() {
        StandardPrefsBinding binding = StandardPrefsBinding.inflate(LayoutInflater.from(getContext()));
        binding.opacitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mView.setOpacity(i, DrawView.Mode.DRAW);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.opacitySeek.setProgress(mView.getOpacity());
        binding.widthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mView.setPaintStrokeWidth(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.widthSeek.setProgress((int) mView.getPaintStrokeWidth());
        mControlButton = binding.prefsControl;
        binding.prefsControl.setOnClickListener(v -> togglePrefsPanel());
        return binding;
    }

    private void showPrefsPanel() {
        ViewUtils.slideInUp(getContext(), binding.prefsView);
        if (mControlButton == null) {
            return;
        }
        if (ThemeUtil.getInstance(getContext()).isDark()) {
            mControlButton.setImageResource(R.drawable.ic_expand_more_white_24dp);
        } else {
            mControlButton.setImageResource(R.drawable.ic_expand_more_black_24dp);
        }
    }

    private void hidePrefsPanel() {
        ViewUtils.slideOutDown(getContext(), binding.prefsView);
        if (mControlButton == null) {
            return;
        }
        if (ThemeUtil.getInstance(getContext()).isDark()) {
            mControlButton.setImageResource(R.drawable.ic_expand_less_white_24dp);
        } else {
            mControlButton.setImageResource(R.drawable.ic_expand_less_black_24dp);
        }
    }

    private void setCurrentColor(IconRadioButton button) {
        if (isFillPicker) {
            binding.currentFillColorButton.setIcon(button.getIcon());
        } else {
            binding.currentStrokeColorButton.setIcon(button.getIcon());
        }
    }

    private void setCurrentTool(IconRadioButton button) {
        binding.currentToolButton.setIcon(button.getIcon());
        binding.currentToolButton.setText(button.getText());
    }

    private void setDrawMode(DrawView.Mode mode, DrawView.Drawer drawer) {
        mView.setMode(mode);
        if (drawer != null) mView.setDrawer(drawer);
        switchPrefsPanel(mode);
    }

    private void showTextPickerDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(getContext());
        RoboEditText editText = new RoboEditText(getContext());
        editText.setHint(R.string.text);
        builder.setView(editText);
        builder.setPositiveButton(R.string.add_text, (dialogInterface, i) -> {
            String text = editText.getText().toString().trim();
            dialogInterface.dismiss();
            setText(text);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(getContext(), R.string.text_is_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        mView.addText(text);
    }

    private void loadImage() {
        NoteImage image = ImageSingleton.getInstance().getItem();
        if (image != null) {
            mView.addBitmap(image.getImage());
        }
        binding.imageButton.setChecked(true);
    }

    @Override
    public NoteImage getImage() {
        ImageSingleton.getInstance().setImage(mView.getBitmapAsByteArray(Bitmap.CompressFormat.PNG, 100));
        return ImageSingleton.getInstance().getItem();
    }

    @Override
    public NoteImage getOriginalImage() {
        return ImageSingleton.getInstance().getItem();
    }

    private int getId(@ColorRes int color) {
        int id = 0;
        switch (color) {
            case R.color.amberPrimary:
                id = R.id.colorAmberButton;
                break;
            case R.color.blackPrimary:
                id = R.id.colorBlackButton;
                break;
            case R.color.bluePrimary:
                id = R.id.colorBlueButton;
                break;
            case R.color.blueLightPrimary:
                id = R.id.colorBlueLightButton;
                break;
            case R.color.cyanPrimary:
                id = R.id.colorCyanButton;
                break;
            case R.color.orangeDeepPrimary:
                id = R.id.colorDeepOrangeButton;
                break;
            case R.color.purpleDeepPrimary:
                id = R.id.colorDeepPurpleButton;
                break;
            case R.color.greenPrimary:
                id = R.id.colorGreenButton;
                break;
            case R.color.greenLightPrimary:
                id = R.id.colorGreenLightButton;
                break;
            case R.color.material_divider:
                id = R.id.colorGreyButton;
                break;
            case R.color.indigoPrimary:
                id = R.id.colorIndigoButton;
                break;
            case R.color.limePrimary:
                id = R.id.colorLimeButton;
                break;
            case R.color.orangePrimary:
                id = R.id.colorOrangeButton;
                break;
            case R.color.pinkPrimary:
                id = R.id.colorPinkButton;
                break;
            case R.color.purplePrimary:
                id = R.id.colorPurpleButton;
                break;
            case R.color.redPrimary:
                id = R.id.colorRedButton;
                break;
            case R.color.yellowPrimary:
                id = R.id.colorYellowButton;
                break;
            case R.color.whitePrimary:
                id = R.id.colorWhiteButton;
                break;
            case R.color.tealPrimary:
                id = R.id.colorTealButton;
                break;
        }
        return id;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTION_REQUEST_GALLERY:
                    addImageFromUri(data.getData());
                    break;
                case Constants.ACTION_REQUEST_CAMERA:
                    getImageFromCamera();
                    break;
            }
        }
    }

    private void addImageFromUri(Uri uri) {
        if (uri == null) return;
        Bitmap bitmapImage = null;
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(getContext(), uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmapImage != null) {
            mView.addBitmap(bitmapImage);
            switchPrefsPanel(DrawView.Mode.IMAGE);
        }
    }

    private void getImageFromCamera() {
        addImageFromUri(mImageUri);
        String pathFromURI = getRealPathFromURI(mImageUri);
        File file = new File(pathFromURI);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isPrefsPanelExpanded()) {
            togglePrefsPanel();
            return true;
        }
        return false;
    }
}
