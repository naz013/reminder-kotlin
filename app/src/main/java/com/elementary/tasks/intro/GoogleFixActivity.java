package com.elementary.tasks.intro;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.ActivityGoogleFixBinding;
import com.elementary.tasks.login.LoginActivity;

public class GoogleFixActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGoogleFixBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_google_fix);
        binding.installButton.setOnClickListener(v -> installServices());
    }

    private void installServices() {
        SuperUtil.checkGooglePlayServicesAvailability(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            openLoginScreen();
        }
    }

    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
