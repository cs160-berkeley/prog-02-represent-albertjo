package com.cs160.joleary.catnip;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class PhoneListenerService extends WearableListenerService {

    //   WearableListenerServices don't need an iBinder or an onStartCommand: they just need an onMessageReceieved.
    private static final String TOAST = "/send_toast";
    private static final String NAME = "/send_name";
    private static final String PARTY = "/send_party";
    private static final String TERM_END_DATE = "/send_term_end_date";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("T", "in PhoneListenerService, got: " + messageEvent.getPath());

        Intent intent;
        if (messageEvent.getPath().equalsIgnoreCase("/launch_congressional_view")) {
            intent = new Intent(getBaseContext(), CongressionalDisplayActivity.class);
            String zip = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            intent.putExtra("zipCode",Integer.parseInt(zip));
            intent.putExtra("intentReceivedFromWear", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (messageEvent.getPath().equalsIgnoreCase("/launch_detail_congressional_view")) {

            intent = new Intent(getBaseContext(), DetailCongressionalDisplayActivity.class);
            // get ID of legislator
            String id = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            intent.putExtra("id",id);
            intent.putExtra("intentReceivedFromWear", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else if (messageEvent.getPath().equalsIgnoreCase("/request_vote_view_data")) {
            // get current zip code
            // get ID of legislator
            String countyState = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            System.out.println(countyState);

            try {
                JsonObject matchingJson = null;

                InputStream stream = getAssets().open("election-county-2012.json");
                int size = stream.available();

                byte[] buffer = new byte[size];
                stream.read(buffer);
                stream.close();
                String jsonString = new String(buffer, "UTF-8");
                JsonParser parser = new JsonParser();
                JsonArray jsonArray = parser.parse(jsonString).getAsJsonArray();

                for (final JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    String _county = jsonObject.get("county-name").getAsString();
                    String _state = jsonObject.get("state-postal").getAsString();
                    String _countyState = _county+":"+_state;
                    if (_countyState.equalsIgnoreCase(countyState)) {
                        matchingJson = jsonObject;
                        break;
                    }

                }

                if (matchingJson !=  null) {
                    String matchingJsonString = matchingJson.toString();
                    intent = new Intent(getBaseContext(), PhoneToWatchVoteInfoService.class);
                    intent.putExtra("json", matchingJsonString);
                    startService(intent);
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        } else if (messageEvent.getPath().equalsIgnoreCase("/request_random_location")) {
            intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("WATCH_RANDOM_LOCATION_REQUEST", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        // fix




        /*
        if( messageEvent.getPath().equalsIgnoreCase(TOAST) ) {

            // Value contains the String we sent over in WatchToPhoneService, "good job"
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);

            // Make a toast with the String
            Context context = getApplicationContext();


            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, value, duration);
            toast.show();


            // so you may notice this crashes the phone because it's
            //''sending message to a Handler on a dead thread''... that's okay. but don't do this.
            // replace sending a toast with, like, starting a new activity or something.
            // who said skeleton code is untouchable? #breakCSconceptions

        } else {
            super.onMessageReceived( messageEvent );
        }
        */
    }
}
