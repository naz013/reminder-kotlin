package com.elementary.tasks.core.views;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;

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

public class ActionView extends LinearLayout {

    public static final int TYPE_CALL = 1;
    public static final int TYPE_MESSAGE = 2;
    private static final int REQ_CONTACTS = 32564;

    private RoboCheckBox actionCheck;
    private LinearLayout actionBlock;
    private RadioGroup radioGroup;
    private RoboRadioButton callAction;
    private RoboRadioButton messageAction;
    private ThemedImageButton selectNumber;
    private RoboEditText numberView;
    private InputMethodManager mImm;

    private Activity mActivity;

    private OnActionListener listener;

    public ActionView(Context context) {
        super(context);
        init(context, null);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.action_view_layout, this);
        setOrientation(VERTICAL);
        actionBlock = findViewById(R.id.actionBlock);
        actionBlock.setVisibility(View.GONE);
        selectNumber = findViewById(R.id.selectNumber);

        numberView = findViewById(R.id.numberView);
        numberView.setFocusableInTouchMode(true);
        numberView.setOnFocusChangeListener((v, hasFocus) -> {
            mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!hasFocus) {
                mImm.hideSoftInputFromWindow(numberView.getWindowToken(), 0);
            } else {
                mImm.showSoftInput(numberView, 0);
            }
        });
        numberView.setOnClickListener(v -> {
            mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!mImm.isActive(numberView)) {
                mImm.showSoftInput(numberView, 0);
            }
        });
        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> buttonClick(i));
        callAction = findViewById(R.id.callAction);
        callAction.setChecked(true);
        messageAction = findViewById(R.id.messageAction);
        actionCheck = findViewById(R.id.actionCheck);
        actionCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!Permissions.checkPermission(mActivity, Permissions.READ_CONTACTS)) {
                actionCheck.setChecked(false);
                Permissions.requestPermission(mActivity, REQ_CONTACTS, Permissions.READ_CONTACTS);
                return;
            }
            if (b) {
                openAction();
            } else {
                ViewUtils.hideOver(actionBlock);
            }
            if (listener != null) {
                listener.onActionChange(b);
            }
        });
        if (actionCheck.isChecked()) {
            openAction();
        }
    }

    private void openAction() {
        ViewUtils.showOver(actionBlock);
        refreshState();
    }

    private void refreshState() {
        buttonClick(radioGroup.getCheckedRadioButtonId());
    }

    private void buttonClick(int i) {
        switch (i) {
            case R.id.callAction:
                if (listener != null) {
                    listener.onTypeChange(false);
                }
                break;
            case R.id.messageAction:
                if (listener != null) {
                    listener.onTypeChange(true);
                }
                break;
        }
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setContactClickListener(OnClickListener contactClickListener) {
        selectNumber.setOnClickListener(contactClickListener);
    }

    public void setListener(OnActionListener listener) {
        this.listener = listener;
    }

    public boolean hasAction() {
        return actionCheck.isChecked();
    }

    public void setAction(boolean action) {
        actionCheck.setChecked(action);
    }

    public int getType() {
        if (hasAction()) {
            if (callAction.isChecked()) {
                return TYPE_CALL;
            } else {
                return TYPE_MESSAGE;
            }
        } else {
            return 0;
        }
    }

    public void setType(int type) {
        if (type == TYPE_CALL) {
            callAction.setChecked(true);
        } else {
            messageAction.setChecked(true);
        }
    }

    public String getNumber() {
        return numberView.getText().toString().trim();
    }

    public void setNumber(String number) {
        numberView.setText(number);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case REQ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actionCheck.setChecked(true);
                }
                break;
        }
    }

    public interface OnActionListener {
        void onActionChange(boolean hasAction);

        void onTypeChange(boolean isMessageType);
    }
}
