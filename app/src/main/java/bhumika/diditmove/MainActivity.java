package bhumika.diditmove;

/**
 * Created by bhumi on 12/3/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    float x,y,z;
    long start_time;
    float mAccelLast, mAccel, mAccelCurrent;
    String result = "";
    TextView resultView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (TextView)findViewById(R.id.result_text);

        startChecking();

        //clear button

        Button clearButton = (Button)findViewById(R.id.clear_button);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = "";
                startChecking();

            }
        });

        //exit button

        Button exitButton = (Button)findViewById(R.id.exit_button);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                moveTaskToBack(true);

            }
        });

        //launchTestService();

    }

    //activate sensor manager

    public void startChecking(){
        resultView.setText("Everything was quiet.");
        start_time = System.currentTimeMillis();
        Toast.makeText(MainActivity.this, "start", Toast.LENGTH_SHORT).show();


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        //Toast.makeText(MainActivity.this, "on sensor changed", Toast.LENGTH_SHORT).show();

        x = sensorEvent.values[0];
        y = sensorEvent.values[1];
        z = sensorEvent.values[2];
//        Log.i("MainActivity", "x: " + x);
//        Log.i("MainActivity", "y: " + y);
//        Log.i("MainActivity", "z: " + z);

        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt(x * x + y * y);
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta;
        //Log.i("MainActivity", "acceleration: " + mAccel);
        // serviceClass.movement(x,y,z);
        if (mAccel >= 1 || mAccel<=-1) {
            launchTestService();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void launchTestService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, ServiceClass.class);
        // Add extras to the bundle

        i.putExtra("sensor_x", x);
        i.putExtra("sensor_y", y);
        i.putExtra("start time", start_time);
        // Start the service
        startService(i);


        //Toast.makeText(MainActivity.this, "second", Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onResume() {
        super.onResume();

        //when the thread is running in the background and the phone was moved

        if(result.equals("moved")){
            resultView.setText("Phone was moved!");
            stopListener();
        }

        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(ServiceClass.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(testReceiver);

    }

    public void stopListener(){
        mSensorManager.unregisterListener(this);
    }


    // the callback for what to do when message is received

    public BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            result = intent.getStringExtra("result");
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            Log.i("MainActivity","result: " + result);

            resultView.setText("The phone was moved!");

            stopListener();


        }
    };
}
