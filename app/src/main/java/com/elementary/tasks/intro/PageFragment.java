package com.elementary.tasks.intro;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.databinding.FragmentIntroPageBinding;

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

public class PageFragment extends Fragment {

    private static final String ARG_POSITION = "arg_position";

    private int position;

    public static Fragment newInstance(int position) {
        PageFragment fragment = new PageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    public PageFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentIntroPageBinding binding = FragmentIntroPageBinding.inflate(inflater, container, false);
        IntroItem item = ItemFactory.getItem(getActivity(), position);
        if (item != null) {
            binding.title.setText(item.getTitle());
            binding.description.setText(item.getDescription());
            binding.imageOne.setImageResource(item.getImages()[0]);
            if (item.getImages().length > 1) {
                binding.imageTwo.setImageResource(item.getImages()[1]);
                binding.imageTwo.setVisibility(View.VISIBLE);
            } else {
                binding.imageTwo.setVisibility(View.INVISIBLE);
            }
        }
        return binding.getRoot();
    }
}
