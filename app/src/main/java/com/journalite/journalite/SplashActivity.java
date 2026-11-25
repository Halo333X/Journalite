// Hecho por Gael
package com.journalite.journalite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openNextScreen = () -> {
        // TODO: change the destination to MainActivity when screen 1 exists
        startActivity(new Intent(this, AddHomeWorkActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(openNextScreen, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openNextScreen);
        super.onDestroy();
    }
}
