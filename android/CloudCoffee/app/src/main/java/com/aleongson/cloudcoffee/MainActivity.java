package com.aleongson.cloudcoffee;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 22/10/2015.
 */
public class MainActivity extends BaseActivity {
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        String feedid = mPrefs.getString("feedid", "");
        String apiKey = mPrefs.getString("apikey", "");
        String user = mPrefs.getString("username", "");

        if(feedid.equals("")) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }

        setContentView(R.layout.activity_main);

        //start service
        //Intent intent = new Intent(this, XivelyTcpService.class);
        //startService(intent);

        TextView username = (TextView) findViewById(R.id.usernameLabel);
        username.setText(user);

        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        Button btnCreateCoffee = (Button) findViewById(R.id.btnCreateCoffee);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.remove("feedid");
                mEditor.remove("apikey");
                mEditor.remove("username");
                mEditor.commit();

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });

        btnCreateCoffee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateCoffeeActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onBackground() {
        //serviceReference.sendNotification();
    }
}