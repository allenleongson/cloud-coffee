package com.aleongson.cloudcoffeeadmin;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.aleongson.cloudcoffeeadmin.async.RetrieveCoffeeMakerStatusTask;

/**
 * 22/10/2015.
 */
public class XivelyTcpService extends Service implements
        RetrieveCoffeeMakerStatusTask.IRetrieveCoffeeMakerStatusListener {
    private final IBinder binder = new LocalBinder();
    private Thread backgroundThread;
    private final static int REQUEST_CODE = 100;
    private final static int NOTIFICATION_ID = 103;
    private boolean hasNotification = false;

    //private boolean hasRequestPending = false;
    //private boolean hasResponsePending = false;
    private boolean isMinimized = false;
    private boolean isRetrieving = false;

    private CoffeeMakerStatus coffeeMakerStatus;

    String feedId;
    String apiKey;

    private Handler timerHandler;
    private Runnable timerRunnable;

    public void setMinimized(boolean b) {
        isMinimized = b;
    }

    @Override
    public void retrieveCoffeeMakerStatusPreExecute() {
        isRetrieving = true;
    }

    @Override
    public void retrieveCoffeeMakerStatusPostExecute(CoffeeMakerStatus result) {
        isRetrieving = false;
        coffeeMakerStatus = result;

        if(isMinimized && coffeeMakerStatus.getErrorCode() != CoffeeMakerStatus.ErrorCode.None) {
            String str = "";
            switch(coffeeMakerStatus.getErrorCode()) {
                case TrayFull:
                    str = "Tray full";
                    break;
                case IngredientShortSupply:
                    str = "Ingredient Short Supply";
                    break;
                case TrayUnaligned:
                    str = "Tray Unaligned";
                    break;
            }
            sendNotification(str, str);
        } else {
            cancelNotification();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("HERE", "test");
        Toast.makeText(this, "Creating Service...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HERE", "Service started by startService()");
        feedId = intent.getStringExtra("feedid");
        apiKey = intent.getStringExtra("apiKey");
        //create runnable here.
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(!isRetrieving) {
                    Log.v("HERE", "runnable in service");
                    new RetrieveCoffeeMakerStatusTask(XivelyTcpService.this, feedId, apiKey).execute();
                }
                timerHandler.postDelayed(this, 5000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 100);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("HERE", "onBind called...");
        return binder;
    }

    public class LocalBinder extends Binder {
        XivelyTcpService getService() {
            return XivelyTcpService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.i("TEST", "DESTROYING SERVICE");
        Toast.makeText(this, "Destroying Service...", Toast.LENGTH_SHORT).show();

        //Thread dummy = backgroundThread;
        //backgroundThread = null;
        //dummy.interrupt();
        timerHandler.removeCallbacks(timerRunnable);
    }

    public CoffeeMakerStatus getCoffeeMakerStatus() {
        return coffeeMakerStatus;
    }

    private void sendNotification(String text, String ticker) {
//        we use the compatibility library
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Cloud Coffee Admin").setContentText(text)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        hasNotification = true;
    }

    public void cancelNotification() {
        if(hasNotification) {
            Log.v("HERE", "HERE");
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Log.i("HERE", "Cancelling notification");
            notificationManager.cancel(NOTIFICATION_ID);
            hasNotification = false;
        }
    }
}
