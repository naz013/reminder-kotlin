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

package com.elementary.tasks.core.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboCheckBox;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;

public class ActionView extends LinearLayout {

    public static final int TYPE_CALL = 1;
    public static final int TYPE_MESSAGE = 2;

    private RoboCheckBox actionCheck;
    private LinearLayout actionBlock;
    private RoboRadioButton callAction, messageAction;
    private ImageButton selectNumber;
    private RoboEditText numberView;
    private InputMethodManager imm;

    private Context mContext;

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
        this.mContext = context;
        if (isInEditMode()) return;
        View.inflate(context, R.layout.action_view_layout, this);
        setOrientation(VERTICAL);
        actionBlock = (LinearLayout) findViewById(R.id.actionBlock);
        actionBlock.setVisibility(View.GONE);
        selectNumber = (ImageButton) findViewById(R.id.selectNumber);
        if (ThemeUtil.getInstance(mContext).isDark()) {
            selectNumber.setImageResource(R.drawable.ic_contacts_white);
        } else {
            selectNumber.setImageResource(R.drawable.ic_contacts);
        }

        numberView = (RoboEditText) findViewById(R.id.numberView);
        numberView.setFocusableInTouchMode(true);
        numberView.setOnFocusChangeListener((v, hasFocus) -> {
            imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!hasFocus) {
                imm.hideSoftInputFromWindow(numberView.getWindowToken(), 0);
            } else {
                imm.showSoftInput(numberView, 0);
            }
        });
        numberView.setOnClickListener(v -> {
            imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive(numberView)){
                imm.showSoftInput(numberView, 0);
            }
        });

        callAction = (RoboRadioButton) findViewById(R.id.callAction);
        callAction.setChecked(true);
        messageAction = (RoboRadioButton) findViewById(R.id.messageAction);
        messageAction.setOnCheckedChangeListener((compoundButton1, b) -> {
            if (listener != null){
                listener.onTypeChange(b);
            }
        });
        actionCheck = (RoboCheckBox) findViewById(R.id.actionCheck);
        actionCheck.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                ViewUtils.showOver(actionBlock);
            } else {
                ViewUtils.hideOver(actionBlock);
            }
            if (listener != null){
                listener.onActionChange(b);
            }
        });
        if (actionCheck.isChecked()) {
            ViewUtils.showOver(actionBlock);
        }
    }

    public void setContactClickListener(OnClickListener contactClickListener) {
        selectNumber.setOnClickListener(contactClickListener);
    }

    public void setListener(OnActionListener listener) {
        this.listener = listener;
    }

    public boolean hasAction(){
        return actionCheck.isChecked();
    }

    public void setAction(boolean action){
        actionCheck.setChecked(action);
    }

    public int getType(){
        if (hasAction()){
            if (callAction.isChecked()){
                return TYPE_CALL;
            } else {
                return TYPE_MESSAGE;
            }
        } else {
            return 0;
        }
    }

    public void setType(int type){
        if (type == TYPE_CALL){
            callAction.setChecked(true);
        } else {
            messageAction.setChecked(true);
        }
    }

    public String getNumber(){
        return numberView.getText().toString().trim();
    }

    public void setNumber(String number){
        numberView.setText(number);
    }

    public interface OnActionListener{
        void onActionChange(boolean hasAction);
        void onTypeChange(boolean isMessageType);
    }
}
