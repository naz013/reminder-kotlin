package com.elementary.tasks.intro;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.ActivityIntroBinding;
import com.elementary.tasks.login.LoginActivity;

public class IntroActivity extends ThemedActivity {

    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro);

        SlidePagerAdapter mPagerAdapter = new SlidePagerAdapter(getFragmentManager());
        binding.viewPager.setAdapter(mPagerAdapter);
        binding.indicator.setViewPager(binding.viewPager);

        binding.skipButton.setOnClickListener(v -> moveToNextScreen());
        binding.nextButton.setOnClickListener(v -> moveForward());
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

    private class SlidePagerAdapter extends FragmentStatePagerAdapter {

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
