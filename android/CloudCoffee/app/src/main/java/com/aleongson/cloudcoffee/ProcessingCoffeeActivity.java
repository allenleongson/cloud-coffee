package com.aleongson.cloudcoffee;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

/**
 * 22/10/2015.
 */
public class ProcessingCoffeeActivity extends BaseActivity {
    private XivelyTcpService serviceReference;
    private boolean isBound;
    private final static int REQUEST_CODE = 100;
    private final static int NOTIFICATION_ID = 103;
    SharedPreferences mPrefs;

    String feedId;
    String apiKey;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private boolean timerRunning = false;
    private int lastResponse = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        feedId = mPrefs.getString("feedid", "");
        apiKey = mPrefs.getString("apikey", "");
        //String user = mPrefs.getString("username", "");

        setContentView(R.layout.activity_processing_coffee);

        //start service
        //assume service started
        //Intent intent = new Intent(this, XivelyTcpService.class);
        //startService(intent);

        /*
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(!serviceReference.hasResponse()) {
                    timerRunning = true;
                    timerHandler.postDelayed(this, 1000);
                } else {
                    //get response.
                    timerRunning = false;
                    timerHandler.removeCallbacks(this);
                    getRequestResponse();
                }
            }
        };
        */
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceReference = ((XivelyTcpService.LocalBinder) service).getService();
            isBound = true;
            serviceReference.cancelNotification();
            //if(timerRunning) {
            //    timerHandler.postDelayed(timerRunnable, 100);
            //}
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
        //timerHandler.removeCallbacks(timerRunnable);
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
