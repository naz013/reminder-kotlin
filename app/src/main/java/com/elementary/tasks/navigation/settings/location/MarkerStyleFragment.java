package com.elementary.tasks.navigation.settings.location;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;
import com.elementary.tasks.databinding.FragmentMarkerStyleLayoutBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

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

public class MarkerStyleFragment extends BaseSettingsFragment {

    private FragmentMarkerStyleLayoutBinding binding;
    private RoboRadioButton red, green, blue, yellow, greenLight, blueLight, cyan, purple,
            amber, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroup4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMarkerStyleLayoutBinding.inflate(inflater, container, false);
        initStyleChecks();
        initGroupViews();
        initActions();
        setUpRadio();
        return binding.getRoot();
    }

    private void initActions() {
        themeGroup.clearCheck();
        themeGroup2.clearCheck();
        themeGroup3.clearCheck();
        themeGroup4.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);
        themeGroup2.setOnCheckedChangeListener(listener2);
        themeGroup3.setOnCheckedChangeListener(listener3);
        themeGroup4.setOnCheckedChangeListener(listener4);
    }

    private void initGroupViews() {
        themeGroup = binding.themeGroup;
        themeGroup2 = binding.themeGroup2;
        themeGroup3 = binding.themeGroup3;
        themeGroup4 = binding.themeGroup4;
    }

    private void initStyleChecks() {
        red = binding.redCheck;
        green = binding.greenCheck;
        blue = binding.blueCheck;
        yellow = binding.yellowCheck;
        greenLight = binding.greenLightCheck;
        blueLight = binding.blueLightCheck;
        cyan = binding.cyanCheck;
        purple = binding.purpleCheck;
        amber = binding.amberCheck;
        orange = binding.orangeCheck;
        pink = binding.pinkCheck;
        teal = binding.tealCheck;
        deepPurple = binding.deepPurpleCheck;
        deepOrange = binding.deepOrangeCheck;
        indigo = binding.indigoCheck;
        lime = binding.limeCheck;
    }

    public void setUpRadio(){
        int loaded = getPrefs().getMarkerStyle();
        if (loaded == 0){
            red.setChecked(true);
        } else if (loaded == 1){
            purple.setChecked(true);
        } else if (loaded == 2){
            greenLight.setChecked(true);
        } else if (loaded == 3){
            green.setChecked(true);
        } else if (loaded == 4){
            blueLight.setChecked(true);
        } else if (loaded == 5){
            blue.setChecked(true);
        } else if (loaded == 6){
            yellow.setChecked(true);
        } else if (loaded == 7){
            orange.setChecked(true);
        } else if (loaded == 8){
            cyan.setChecked(true);
        } else if (loaded == 9){
            pink.setChecked(true);
        } else if (loaded == 10){
            teal.setChecked(true);
        } else if (loaded == 11){
            amber.setChecked(true);
        } else if (loaded == 12){
            deepPurple.setChecked(true);
        } else if (loaded == 13){
            deepOrange.setChecked(true);
        } else if (loaded == 14){
            lime.setChecked(true);
        } else if (loaded == 15){
            indigo.setChecked(true);
        } else {
            blue.setChecked(true);
        }
    }

    private void themeColorSwitch(int radio){
        switch (radio){
            case R.id.redCheck:
                saveColor(0);
                break;
            case R.id.purpleCheck:
                saveColor(1);
                break;
            case R.id.greenLightCheck:
                saveColor(2);
                break;
            case R.id.greenCheck:
                saveColor(3);
                break;
            case R.id.blueLightCheck:
                saveColor(4);
                break;
            case R.id.blueCheck:
                saveColor(5);
                break;
            case R.id.yellowCheck:
                saveColor(6);
                break;
            case R.id.orangeCheck:
                saveColor(7);
                break;
            case R.id.cyanCheck:
                saveColor(8);
                break;
            case R.id.pinkCheck:
                saveColor(9);
                break;
            case R.id.tealCheck:
                saveColor(10);
                break;
            case R.id.amberCheck:
                saveColor(11);
                break;
            case R.id.deepPurpleCheck:
                saveColor(12);
                break;
            case R.id.deepOrangeCheck:
                saveColor(13);
                break;
            case R.id.limeCheck:
                saveColor(14);
                break;
            case R.id.indigoCheck:
                saveColor(15);
                break;
        }
    }

    void saveColor(int style) {
        getPrefs().setMarkerStyle(style);
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.style_of_marker));
            getCallback().onFragmentSelect(this);
        }
    }
}
