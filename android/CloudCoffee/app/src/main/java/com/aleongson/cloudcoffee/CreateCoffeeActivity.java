package com.aleongson.cloudcoffee;

import android.app.ProgressDialog;
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
public class CreateCoffeeActivity extends BaseActivity {
    private XivelyTcpService serviceReference;
    private boolean isBound;
    private final static int REQUEST_CODE = 100;
    private final static int NOTIFICATION_ID = 103;
    SharedPreferences mPrefs;

    String feedId;
    String apiKey;

    ProgressDialog progress;

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

        setContentView(R.layout.activity_create_coffee);

        //start service
        Intent intent = new Intent(this, XivelyTcpService.class);
        startService(intent);

        final NumberPicker numCoffee = (NumberPicker) findViewById(R.id.numCoffee);
        final NumberPicker numSugar = (NumberPicker) findViewById(R.id.numSugar);
        final NumberPicker numCreamer = (NumberPicker) findViewById(R.id.numCreamer);
        Button btnSendCoffeeRequest = (Button) findViewById(R.id.btnSendCoffeeRequest);

        numCoffee.setMinValue(1);
        numCoffee.setMaxValue(3);

        numSugar.setMinValue(0);
        numSugar.setMaxValue(3);

        numCreamer.setMinValue(0);
        numCreamer.setMaxValue(3);

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

        btnSendCoffeeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceReference.sendCoffeeRequest(feedId, apiKey, numCoffee.getValue(), numSugar.getValue(), numCreamer.getValue());
                //runnable check status.
                timerHandler.postDelayed(timerRunnable, 100);
                timerRunning = true;
                //progress bar
                progress = ProgressDialog.show(CreateCoffeeActivity.this, "Please Wait...",
                        "Sending your request...");
            }
        });
    }

    private void getRequestResponse() {
        progress.dismiss();
        int response = serviceReference.getResponseCode();
        switch(response) {
            case -1:
                //Coffee maker offline.
                Log.v("HERE", "Offline.");
                Toast.makeText(getApplicationContext(), "Offline.",
                        Toast.LENGTH_SHORT).show();
                break;
            case 0:
                Log.v("HERE", "No Error.");
                Intent intent = new Intent(CreateCoffeeActivity.this, ProcessingCoffeeActivity.class);
                startActivity(intent);
                CreateCoffeeActivity.this.finish();
                //no error. open activity.
                break;
            case 1:
                Log.v("HERE", "Tray Malfunction.");
                Toast.makeText(getApplicationContext(), "Tray Malfunction.",
                        Toast.LENGTH_SHORT).show();
                //malfunction
                break;
            case 2:
                Log.v("HERE", "Short Ingredient supply.");
                Toast.makeText(getApplicationContext(), "Short Ingredient Supply.",
                        Toast.LENGTH_SHORT).show();
                //short ingredient supply.
                break;
            case 3:
                Log.v("HERE", "Full Tray.");
                Toast.makeText(getApplicationContext(), "Full. Try again later..",
                        Toast.LENGTH_SHORT).show();
                //full tray.
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceReference = ((XivelyTcpService.LocalBinder) service).getService();
            isBound = true;
            serviceReference.cancelNotification();
            if(timerRunning) {
                timerHandler.postDelayed(timerRunnable, 100);
            }
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
        timerHandler.removeCallbacks(timerRunnable);
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
