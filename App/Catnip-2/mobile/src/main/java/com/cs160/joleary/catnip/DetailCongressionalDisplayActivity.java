package com.cs160.joleary.catnip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.ExpandableListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cs160.joleary.catnip.Classes.Bill;
import com.cs160.joleary.catnip.Classes.Committee;
import com.cs160.joleary.catnip.Classes.Legislator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class DetailCongressionalDisplayActivity extends Activity {
    Legislator legislator;
    Boolean intentReceivedFromWear;
    ArrayList<Committee> committees = new ArrayList<Committee>();
    ArrayList<Bill> bills = new ArrayList<Bill>();
    private static String sunlightKey = "43f6aef9e91647498c1ace42c07993fe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_congressional_display);
        this.intentReceivedFromWear = getIntent().getBooleanExtra("intentReceivedFromWear", false);
        // if intent received from wear, need to query Sunlight for Legislator
        if (intentReceivedFromWear) {
            String id = getIntent().getExtras().getString("id");
            retrieveLegislatorFromIdAndUpdateViews(id);
        } else {
            legislator = getIntent().getExtras().getParcelable("legislator");
            updateViews();
        }
        // set color of action bar
        // set Action bar properties (bar color + title + text color)


    }

    public void updateViews() {
        String colorStr;
        switch (legislator.party) {
            case "D": colorStr = "#19B5FE"; break;
            case "R": colorStr = "#F64747"; break;
            default:  colorStr = "#BF55EC";
        }
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorStr)));
        getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>" + legislator.getFullTitleName() + "</font>"));
        getCommitteesAndUpdateView();
    }

    public void getCommitteesAndUpdateView() {
        String urlFormatStr = "https://congress.api.sunlightfoundation.com/committees?member_ids=%s&apikey=%s";
        String url = String.format(urlFormatStr, legislator.id, sunlightKey);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
                        JsonArray resultsJsonArray = jsonObject.getAsJsonArray("results");
                        for(final JsonElement jsonElement : resultsJsonArray) {
                            committees.add(new Committee(jsonElement.getAsJsonObject()));
                        }

                        getRecentBillsAndUpdateView();
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

    public void getRecentBillsAndUpdateView() {
        String urlFormatStr = "https://congress.api.sunlightfoundation.com/bills?sponsor_id=%s&apikey=%s";
        String url = String.format(urlFormatStr, legislator.id, sunlightKey);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
                        JsonArray resultsJsonArray = jsonObject.getAsJsonArray("results");
                        for(final JsonElement jsonElement : resultsJsonArray) {
                            bills.add(new Bill(jsonElement.getAsJsonObject()));
                        }
                        updateListView();
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

    public void updateListView() {
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        ExpandableListAdapter listAdapter = new ExpandableListAdapter(this, committees, bills, legislator);
        expandableListView.setAdapter(listAdapter);
    }

    public void retrieveLegislatorFromIdAndUpdateViews(String id) {
        String urlFormatStr = "https://congress.api.sunlightfoundation.com/legislators?bioguide_id=%s&apikey=%s";
        String url = String.format(urlFormatStr, id,sunlightKey);
        System.out.println(url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonParser parser = new JsonParser();
                        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
                        JsonArray resultsJsonArray = jsonObject.getAsJsonArray("results");
                        for(final JsonElement jsonElement : resultsJsonArray) {
                            legislator = Legislator.legislatorFromJson(jsonElement.getAsJsonObject());
                        }
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
}
