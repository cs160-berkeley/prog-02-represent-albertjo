package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "q3HRKhqm8smAlCsweqYoKBHZv";
    private static final String TWITTER_SECRET = "Bk6s7r5SX33S9P86jBDGZhoudxWbGLnyWKPi8JaHngPf8aDwRb";

    //there's not much interesting happening. when the buttons are pressed, they start
    //the PhoneToWatchService with the cat name passed in.
    private GoogleApiClient mGoogleApiClient;
    String apiKey = "AIzaSyDFRxueLIbMpP4lCjjK9sooUojCEI_mYl0";

    Boolean calledByRandomLocationRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        setContentView(R.layout.activity_main);
        getActionBar().hide();

        calledByRandomLocationRequest = getIntent().getBooleanExtra("WATCH_RANDOM_LOCATION_REQUEST", false);
        if (calledByRandomLocationRequest == true) {
            getRandomLocationAndLaunchCongressionalView();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        final EditText zipCodeEditText = (EditText) findViewById(R.id.zipCodeEditText);
        zipCodeEditText.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //...
                    // Perform your action on key press here
                    // ...
                    int zipCode = Integer.parseInt(zipCodeEditText.getText().toString());
                    getInfoFromZipCodeAndLaunchCongressionalView(zipCode);
                    return true;
                }
                return false;
            }
        });


    }

    private void getRandomLocationAndLaunchCongressionalView() {
        // America is approx 27 <= longitude <= 47
        // and 125 >= latitude >= 70
        Random ran = new Random();
        double latitude = 27 + ran.nextInt(47-27+1)+Math.random();
        double longitude = -1*(70 + ran.nextInt(125-79+1)+Math.random());

        DecimalFormat formatter = new DecimalFormat("#.0##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        formatter.setRoundingMode(RoundingMode.DOWN);

        String strLongitude = formatter.format(longitude);
        String strLatitude = formatter.format(latitude);

        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&result_type=postal_code&key=%s", strLatitude, strLongitude, apiKey);
        System.out.println(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        // check if in America
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
                        JsonArray resultsArray = jsonObject.getAsJsonArray("results").getAsJsonArray();
                        if (resultsArray.size() != 0) {
                            int zipCode = parseJSONStringForZipCode(response);
                            String county = parseJSONStringForCounty(response);
                            String state = parseJSONStringForState(response);
                            launchCongressionalView(zipCode, county, state);
                        } else {
                            // recursively try again
                            getRandomLocationAndLaunchCongressionalView();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                });
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void getInfoFromZipCodeAndLaunchCongressionalView(final int zipCode) {
        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%d&key=%s", zipCode,apiKey);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String county = parseJSONStringForCounty(response);
                        String state = parseJSONStringForState(response);
                        launchCongressionalView(zipCode, county, state);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                });
        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchCongressionalView(int zipCode, String county, String state) {
        Intent intent = new Intent(this, CongressionalDisplayActivity.class);
        intent.putExtra("zipCode",zipCode);
        intent.putExtra("county", county);
        intent.putExtra("state", state);
        startActivity(intent);
    }

    public void launchCongressionalViewWithCurrentLocation(View view) {
        //getRandomLocationAndLaunchCongressionalView();
        mGoogleApiClient.connect();
    }

    /*
    These functions are necessary for Google API Client callbacks & error handling
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override //alternate method to connecting: no longer create this in a new thread, but as a callback
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&result_type=postal_code&key=%s", latitude, longitude, apiKey);
            System.out.println(url);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            int zipCode = parseJSONStringForZipCode(response);
                            String county = parseJSONStringForCounty(response);
                            String state = parseJSONStringForState(response);
                            launchCongressionalView(zipCode, county, state);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error);
                        }
            });
            Volley.newRequestQueue(this).add(stringRequest);
        }
    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult result) { }

    // parses google reverse geocoding json for zip code
    public static int parseJSONStringForZipCode(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        JsonObject resultsJSON = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject();
        JsonObject zipComponentJSON = resultsJSON.getAsJsonArray("address_components").get(0).getAsJsonObject();
        int zipCode = zipComponentJSON.get("long_name").getAsInt();
        return zipCode;
    }

    public static String parseJSONStringForCounty(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        JsonObject resultsJSON = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject();
        JsonArray addressComponents = resultsJSON.getAsJsonArray("address_components");
        JsonObject countyJSON = addressComponents.get(2).getAsJsonObject();
        String countyString = countyJSON.get("long_name").getAsString();
        countyString = countyString.replace(" County", "");
        return countyString;
    }

    public static String parseJSONStringForState(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        JsonObject resultsJSON = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject();

        JsonArray addressComponents = resultsJSON.getAsJsonArray("address_components");
        JsonObject stateJSON = addressComponents.get(3).getAsJsonObject();
        String stateString = stateJSON.get("short_name").getAsString();
        return stateString;
    }
}
