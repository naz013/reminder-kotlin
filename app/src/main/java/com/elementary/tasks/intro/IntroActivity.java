package com.elementary.tasks.intro;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.ActivityIntroBinding;
import com.elementary.tasks.login.LoginActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class IntroActivity extends ThemedActivity {

    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro);

        SlidePagerAdapter mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(mPagerAdapter);
        binding.indicator.setViewPager(binding.viewPager);

        binding.skipButton.setOnClickListener(v -> moveToNextScreen());
        binding.nextButton.setOnClickListener(v -> moveForward());
        if (getThemeUtil().isDark()) {
            binding.nextButton.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
        } else {
            binding.nextButton.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
        }
    }

    private void moveForward() {
        if (binding.viewPager.getCurrentItem() >= 4) {
            moveToNextScreen();
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        }
    }

    private void moveToNextScreen() {
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            openLoginScreen();
        } else {
            openFixScreen();
        }
    }

    private void openFixScreen() {
        startActivity(new Intent(this, GoogleFixActivity.class));
        finish();
    }

    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1);
        }
    }

    private class SlidePagerAdapter extends FragmentPagerAdapter {

        SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
