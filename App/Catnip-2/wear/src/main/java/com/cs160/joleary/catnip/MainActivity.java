package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.Random;

public class MainActivity extends Activity {

    int zipCode = 94704;
    /* put this into your activity class */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private  SensorEventListener mSensorListener;

    private void reset() {
        mAccel = 0f;
        mAccelCurrent = 0f;
        mAccelLast= 0f;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent se) {
                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
                float delta = mAccelCurrent - mAccelLast;
                mAccel = mAccel * 0.9f + delta; // perform low-cut filter

                if (mAccel > 12) {
                    Random r = new Random();
                    int randomZipCode = r.nextInt(100000 - 10000) + 10000;
                    System.out.println(randomZipCode);

                    Intent sendIntent = new Intent(getBaseContext(), WatchToPhoneCongressionalViewService.class);
                    sendIntent.putExtra("zipCode", randomZipCode);
                    getBaseContext().startService(sendIntent);

                    Intent intent = new Intent(getBaseContext(), GridViewFragmentActivity.class);
                    intent.putExtra("zipCode", randomZipCode);
                    startActivity(intent);
                    reset();
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;



        /*
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        */

        final EditText zipCodeEditText = (EditText) findViewById(R.id.zipCodeEditText);
        zipCodeEditText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    zipCode = Integer.parseInt(zipCodeEditText.getText().toString());
                    launchMainView(view);

                    return true;
                }
                return false;
            }
        });

        /*final ImageButton locationButton = (ImageButton) findViewById(R.id.useLocationButton);
        locationButton.setOnClickListener(
        );*/

    }

    public void launchMainViewMyLocation(View v) {
        this.zipCode=94704;
        launchMainView(v);
    }



    public void launchMainView(View view) {
        Intent sendIntent = new Intent(this, WatchToPhoneCongressionalViewService.class);
        sendIntent.putExtra("zipCode", this.zipCode);
        this.startService(sendIntent);

        Intent intent = new Intent(this, GridViewFragmentActivity.class);
        intent.putExtra("zipCode", this.zipCode);
        startActivity(intent);
    }
}


