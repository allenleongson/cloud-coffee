package com.aleongson.cloudcoffee;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 22/10/2015.
 */
public class MainActivity extends BaseActivity {
    private XivelyTcpService serviceReference;
    private boolean isBound;
    private final static int REQUEST_CODE = 100;
    private final static int NOTIFICATION_ID = 103;
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
        Intent intent = new Intent(this, XivelyTcpService.class);
        startService(intent);

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
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceReference = ((XivelyTcpService.LocalBinder) service).getService();
            isBound = true;
            serviceReference.cancelNotification();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceReference = null;
            isBound = false;
        }
    };

    private void doUnbindService() {
        unbindService(connection);
        isBound = false;
    }

    private void doBindToService() {
        if(!isBound) {
            Intent bindIntent = new Intent(this, XivelyTcpService.class);
            isBound = bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doBindToService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        if(isFinishing()) {
            Intent intentStopService = new Intent(this, XivelyTcpService.class);
            stopService(intentStopService);
        }*/
    }

    @Override
    protected void onBackground() {
        serviceReference.sendNotification();
    }
}