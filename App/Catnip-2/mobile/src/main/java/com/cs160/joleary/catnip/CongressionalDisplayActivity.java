package com.cs160.joleary.catnip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cs160.joleary.catnip.Classes.Legislator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

/**
 * Created by namhyun on 3/1/16.
 */
public class CongressionalDisplayActivity extends Activity {
    int zipCode;
    String county;
    String state;
    String sunlightKey = "43f6aef9e91647498c1ace42c07993fe";
    ArrayList<Legislator> legislators = new ArrayList<Legislator>();
    Boolean intentReceivedFromWear = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.congressional_display);

        // retrieve zip code
        this.zipCode = getIntent().getIntExtra("zipCode", 0);
        this.county = getIntent().getStringExtra("county");
        this.state = getIntent().getStringExtra("state");


        // set Action bar properties (bar color + title + text color)
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#19B5FE")));
        getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Congress members for "+zipCode+ "</font>"));
        getLegislatorsAndUpdateViews();
    }


    public void getLegislatorsAndUpdateViews() {
        String urlFormatStr = "https://congress.api.sunlightfoundation.com/legislators/locate?zip=%d&apikey=%s";
        String url = String.format(urlFormatStr, zipCode, sunlightKey);
        System.out.println(url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
                        JsonArray resultsJsonArray = jsonObject.getAsJsonArray("results");

                        for(final JsonElement jsonElement : resultsJsonArray) {
                            Legislator legislator = Legislator.legislatorFromJson(jsonElement.getAsJsonObject());
                            legislator.zipCodeString = Integer.toString(zipCode);
                            legislator.county = county;
                            legislator.state = state;
                            legislators.add(legislator);
                        }

                        //update watch views
                        updateWatchViews();
                        // updateViews
                        updateViews();


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

    private void updateViews() {
        final ListView listView = (ListView)findViewById(R.id.listView);
        final CustomListAdapter adapter = new CustomListAdapter(this, legislators);

        // access guest API for Twitter
        TwitterAuthConfig authConfig = new TwitterAuthConfig("q3HRKhqm8smAlCsweqYoKBHZv",
                                        "Bk6s7r5SX33S9P86jBDGZhoudxWbGLnyWKPi8JaHngPf8aDwRb");
        Fabric.with(this, new Twitter(authConfig));
        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> result) {
                AppSession guestAppSession = result.data;
                adapter.guestAppSession = guestAppSession;
                listView.setAdapter(adapter);
            }

            @Override
            public void failure(TwitterException exception) {
                // unable to get an AppSession with guest auth
                throw exception;
            }
        });
    }

    // sends an intent to update watch
    private void updateWatchViews() {
        Intent sendIntent = new Intent(this, PhoneToWatchService.class);
        sendIntent.putParcelableArrayListExtra("legislators", legislators);

        this.startService(sendIntent);
    }
}
