package com.cs160.joleary.catnip;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, fred vs lexy)
        Intent intent;
        // intent to update watch view w/ legislators
        if (messageEvent.getPath().equalsIgnoreCase("/update_watch_view")) {
            String jsonString = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray resultsJsonArray = parser.parse(jsonString).getAsJsonArray();

            ArrayList<Legislator> legislators = new ArrayList<Legislator>();
            for(final JsonElement jsonElement : resultsJsonArray) {
                legislators.add(gson.fromJson(jsonElement, Legislator.class));
            }

            intent = new Intent(getBaseContext(), GridViewFragmentActivity.class);
            intent.putParcelableArrayListExtra("legislators", legislators);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (messageEvent.getPath().equalsIgnoreCase("/vote_view_data")) {
            String jsonString = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            intent = new Intent(getBaseContext(), VoteViewActivity.class);
            intent.putExtra("json", jsonString);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}