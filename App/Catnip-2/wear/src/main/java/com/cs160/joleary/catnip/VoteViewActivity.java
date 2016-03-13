package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class VoteViewActivity extends Activity {
    VoteData voteData;
    int zipCode;

    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    private SensorEventListener mSensorListener;

    private void reset() {
        mAccel = 0f;
        mAccelCurrent = 0f;
        mAccelLast= 0f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        Bundle extras = getIntent().getExtras();
        String jsonString = extras.getString("json");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        voteData = new VoteData(jsonObject);

        TextView countyStateTextView = (TextView) findViewById(R.id.countyStateTextView);
        TextView obamaVotePercentageTextView = (TextView) findViewById(R.id.obamaVotePercentageTextView);
        TextView romneyVotePercentageTextView = (TextView) findViewById(R.id.rommneyVotePercentageTextView);

        countyStateTextView.setText(voteData.county+" - "+voteData.state);
        obamaVotePercentageTextView.setText(Float.toString(voteData.obamaPercentage)+"%");
        romneyVotePercentageTextView.setText(Float.toString(voteData.romneyPercentage)+"%");

        // init sensor with callback function
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
                    Intent intent = new Intent(getBaseContext(), RequestRandomZipCodeService.class);
                    startService(intent);
                    reset();
                }
            }
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private class VoteData {
        String state;
        String county;
        int fips;
        int obamaVotes;
        float obamaPercentage;
        int romneyVotes;
        float romneyPercentage;

        public VoteData(JsonObject jsonObject) {
            state=jsonObject.get("state-postal").getAsString();
            county=jsonObject.get("county-name").getAsString();
            fips=jsonObject.get("fips").getAsInt();
            obamaVotes=jsonObject.get("obama-vote").getAsInt();
            romneyVotes=jsonObject.get("obama-vote").getAsInt();

            obamaPercentage=jsonObject.get("obama-percentage").getAsFloat();
            romneyPercentage=jsonObject.get("romney-percentage").getAsFloat();
        }
    }
}
