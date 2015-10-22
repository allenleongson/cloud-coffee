package com.aleongson.cloudcoffee;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * 22/10/2015.
 */
public class MainActivity extends AppCompatActivity {
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        String feedid = mPrefs.getString("feedid", "");
        String apiKey = mPrefs.getString("apikey", "");
        String user = mPrefs.getString("username", "");

        setContentView(R.layout.activity_main);
        TextView username = (TextView) findViewById(R.id.usernameLabel);
        username.setText(user);
    }
}