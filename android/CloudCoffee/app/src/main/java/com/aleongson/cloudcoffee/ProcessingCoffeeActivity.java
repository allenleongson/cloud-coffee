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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
    private Handler waitCoffeeHandler;
    private Runnable waitCoffeeRunnable;

    private boolean timerRunning = false;
    private boolean waitRunning = false;
    private int lastResponse = -1;

    TextView titleText;
    TextView messageText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        feedId = mPrefs.getString("feedid", "");
        apiKey = mPrefs.getString("apikey", "");
        //String user = mPrefs.getString("username", "");

        setContentView(R.layout.activity_processing_coffee);

        titleText = (TextView) findViewById(R.id.txtProcessing1);
        messageText = (TextView) findViewById(R.id.txtProcessing2);
        progressBar = (ProgressBar) findViewById(R.id.progressProcessing);
        //start service
        //assume service started
        //Intent intent = new Intent(this, XivelyTcpService.class);
        //startService(intent);
        waitCoffeeHandler = new Handler();
        waitCoffeeRunnable = new Runnable() {
            @Override
            public void run() {
                if(isBound) {
                    serviceReference.waitCoffeeFinished(feedId, apiKey);
                    waitCoffeeHandler.removeCallbacks(this);
                    waitRunning = false;
                    //start timer runnable
                    timerHandler.postDelayed(timerRunnable, 100);
                } else {
                    waitCoffeeHandler.postDelayed(this, 1000);
                }
            }
        };

        //run
        waitRunning = true;
        waitCoffeeHandler.postDelayed(waitCoffeeRunnable, 100);

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
    }

    private void getRequestResponse() {
        int response = serviceReference.getResponseCode();
        titleText.setText("Tray " + Integer.toString(response + 1));
        messageText.setText("Your coffee is ready. Thank you.");
        progressBar.setVisibility(View.INVISIBLE);

        Button b = new Button(this);
        b.setText("Ok");

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeProcessingCoffee);
        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.BELOW, R.id.txtProcessing2);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.setMargins(0, 54, 0, 0);
        rl.addView(b, lp);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProcessingCoffeeActivity.this, MainActivity.class);
                startActivity(intent);
                ProcessingCoffeeActivity.this.finish();
            }
        });
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceReference = ((XivelyTcpService.LocalBinder) service).getService();
            isBound = true;
            serviceReference.cancelNotification();
            serviceReference.setMinimized(false);
            if(waitRunning) {
                waitCoffeeHandler.postDelayed(waitCoffeeRunnable, 100);
            }
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
        waitCoffeeHandler.removeCallbacks(waitCoffeeRunnable);
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
        if(isFinishing()) {
            Intent intentStopService = new Intent(this, XivelyTcpService.class);
            stopService(intentStopService);
        }
    }

    @Override
    protected void onBackground() {
        //serviceReference.sendNotification();
        serviceReference.setMinimized(true);
    }
}
