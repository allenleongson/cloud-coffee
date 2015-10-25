package com.aleongson.cloudcoffeeadmin;

import android.support.v7.app.AppCompatActivity;

/**
 * 22/10/2015.
 */
public class BaseActivity extends AppCompatActivity {
    private static int sessionDepth = 0;

    @Override
    protected void onStart() {
        super.onStart();
        sessionDepth++;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sessionDepth > 0)
            sessionDepth--;
        if (sessionDepth == 0) {
            onBackground();
        }
    }

    protected void onBackground() {

    }
}
