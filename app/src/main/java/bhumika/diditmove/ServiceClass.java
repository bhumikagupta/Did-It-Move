package bhumika.diditmove;

/**
 * Created by bhumi on 12/3/2017.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;




public class ServiceClass extends Service {

    private PowerManager.WakeLock mWakeLock;

    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;
    long acc_time;
    boolean moved = false;
    long start_time;
    float last_acc_time;


    public static final String ACTION = "ServiceClass";
    private LocalBroadcastManager mLocalBroadcastManager;

    // Define how the handler will process messages

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Define how to handle any incoming messages here
        @Override
        public void handleMessage(Message message) {
            // ...
            // When needed, stop the service with
            // stopSelf();
        }
    }

    // Fires when a service is first initialized

    public void onCreate() {
        Log.i("ServiceClass","The Service");


        super.onCreate();
        // An Android handler thread internally operates on a looper.
        mHandlerThread = new HandlerThread("ServiceClass.HandlerThread");
        mHandlerThread.start();
        // An Android service handler is a handler running on a specific background thread.
        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());



    }

    //Fires when a service is started up

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        mWakeLock.acquire();

        acc_time = System.currentTimeMillis();
        start_time = intent.getLongExtra("start time", -1);


        //Log.i("ServiceClass", "time: " + start_time);

        mServiceHandler.post(new Runnable() {


            @Override
            public void run() {

//                Log.i("ServiceClass","first time:" + acc_time);
               Log.i("ServiceClass","start time:" + start_time);

                //TODO: if the phone was moved after 30 seconds from start time

                if(acc_time != 0 && (acc_time - start_time) >=30000){
                    last_acc_time = acc_time;
                    moved = true;
                }
                if(moved == true) {

                    //TODO: when the phone is moved don't detect for the next 30 seconds to give user time to check
                    try {
                        Thread.sleep(30000);
                    }
                    catch (InterruptedException e){

                    }

                    //TODO: if the phone was moved, notify activity and kill thread
                    Log.i("ServiceClass","moved");
                    stopSelf();
                    // Send broadcast out with action filter and extras
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("result", "moved");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    // If desired, stop the service

                }
            }
        });
        // Keep service around "sticky"
        return START_STICKY;
    }

    // Defines the shutdown sequence
    @Override
    public void onDestroy() {
        // Cleanup service before destruction
        mHandlerThread.quit();
        mWakeLock.release();
    }

    // Binding is another way to communicate between service and activity
    // Not needed here, local broadcasts will be used instead
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


