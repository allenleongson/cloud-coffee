package com.aleongson.cloudcoffeeadmin;

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
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 23/10/2015.
 */
public class MainActivity extends BaseActivity {
    SharedPreferences mPrefs;

    String feedid;
    String apiKey;
    String user;

    private XivelyTcpService serviceReference;
    private boolean isBound;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private boolean timerRunning = false;
    private boolean updated = false;

    private CoffeeMakerStatus coffeeMakerStatus;

    private TextView coffeeTextView;
    private TextView sugarTextView;
    private TextView creamerTextView;
    private TextView waterTextView;
    private TextView statusTextView;
    private TextView trayTextView[];

    private ProgressBar coffeeProgressBar;
    private ProgressBar sugarProgressBar;
    private ProgressBar creamerProgressBar;
    private ProgressBar waterProgressBar;

    private ProgressDialog progressDialog;

    private void retrieveCoffeeMakerStatus() {
        coffeeMakerStatus = serviceReference.getCoffeeMakerStatus();
        if(!updated && coffeeMakerStatus != null) {
            progressDialog.dismiss();
        }

        if(coffeeMakerStatus != null) {
            coffeeTextView.setText("Coffee: (" + coffeeMakerStatus.getCoffeeTsp() + "/" + CoffeeMakerStatus.MAX_COFFEE_TSP + ")");
            coffeeProgressBar.setProgress(coffeeMakerStatus.getCoffeeTsp());

            sugarTextView.setText("Sugar: (" + coffeeMakerStatus.getSugarTsp() + "/" + CoffeeMakerStatus.MAX_SUGAR_TSP + ")");
            sugarProgressBar.setProgress(coffeeMakerStatus.getSugarTsp());

            creamerTextView.setText("Creamer: (" + coffeeMakerStatus.getCreamerTsp() + "/" + CoffeeMakerStatus.MAX_CREAMER_TSP + ")");
            creamerProgressBar.setProgress(coffeeMakerStatus.getCreamerTsp());

            waterTextView.setText("Water: (" + coffeeMakerStatus.getWaterCup() + "/" + CoffeeMakerStatus.MAX_WATER_CUP + ")");
            waterProgressBar.setProgress(coffeeMakerStatus.getWaterCup());

            CoffeeMakerStatus.ErrorCode errorCode = coffeeMakerStatus.getErrorCode();
            String s = "Running";
            switch(errorCode) {
                case None:
                    s = "Running";
                    break;
                case TrayUnaligned:
                    s = "Tray Unaligned";
                    break;
                case IngredientShortSupply:
                    s = "Ingredient Short Supply";
                    break;
                case TrayFull:
                    s = "Tray full";
                    break;
            }
            statusTextView.setText(s);

            for(int i = 0; i < 3; i++) {
                String str = "Vacant";
                if(coffeeMakerStatus.getTrayStatus(i) != CoffeeMakerStatus.TrayStatus.Vacant) {
                    str = coffeeMakerStatus.getTrayOwner(i);
                }
                trayTextView[i].setText(str);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("login", 0);
        feedid = mPrefs.getString("feedid", "");
        apiKey = mPrefs.getString("apikey", "");
        user = mPrefs.getString("username", "");

        if(feedid.equals("")) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            MainActivity.this.finish();
        }

        setContentView(R.layout.activity_main);

        //start service.
        Intent intent = new Intent(this, XivelyTcpService.class);
        intent.putExtra("feedid", feedid);
        intent.putExtra("apiKey", apiKey);
        startService(intent);

        //get widgets
        coffeeTextView = (TextView) findViewById(R.id.tvCoffee);
        sugarTextView = (TextView) findViewById(R.id.tvSugar);
        creamerTextView = (TextView) findViewById(R.id.tvCreamer);
        waterTextView = (TextView) findViewById(R.id.tvWater);

        statusTextView = (TextView) findViewById(R.id.tvStatus);
        trayTextView = new TextView[3];
        trayTextView[0] = (TextView) findViewById(R.id.tvTray1);
        trayTextView[1] = (TextView) findViewById(R.id.tvTray2);
        trayTextView[2] = (TextView) findViewById(R.id.tvTray3);

        coffeeProgressBar = (ProgressBar) findViewById(R.id.progressCoffee);
        coffeeProgressBar.setMax(CoffeeMakerStatus.MAX_COFFEE_TSP);
        sugarProgressBar = (ProgressBar) findViewById(R.id.progressSugar);
        sugarProgressBar.setMax(CoffeeMakerStatus.MAX_SUGAR_TSP);
        creamerProgressBar = (ProgressBar) findViewById(R.id.progressCreamer);
        creamerProgressBar.setMax(CoffeeMakerStatus.MAX_CREAMER_TSP);
        waterProgressBar = (ProgressBar) findViewById(R.id.progressWater);
        waterProgressBar.setMax(CoffeeMakerStatus.MAX_WATER_CUP);

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerRunning = true;
                retrieveCoffeeMakerStatus();
                timerHandler.postDelayed(this, 5000);
            }
        };

        timerRunning = true;

        TextView usernameLabel = (TextView) findViewById(R.id.tvUsernameLabel);
        TextView feedIdLabel = (TextView) findViewById(R.id.tvFeedIdLabel);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);

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

        usernameLabel.setText(user);
        feedIdLabel.setText(feedid);

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
        if(!updated) {
            progressDialog = ProgressDialog.show(MainActivity.this, "Please Wait...",
                    "Getting status.");
        }
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
        //serviceReference.sendNotification();
    }
}
