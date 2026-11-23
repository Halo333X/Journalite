// Hecho por Gael
package com.journalite.journalite;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread timer = new Thread() {
            public void run() {
                super.run();
                try {
                    sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent openMainWindow = new Intent();
                    openMainWindow.setClassName("com.journalite.journalite", "com.journalite.journalite.AddHomeWorkActivity");
                    startActivity(openMainWindow);
                    finish();
                }
            }
        };
        timer.start();
    }
}