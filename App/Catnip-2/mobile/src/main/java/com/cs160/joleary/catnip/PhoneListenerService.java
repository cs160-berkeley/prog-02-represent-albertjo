package com.cs160.joleary.catnip;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

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
        } else /*if (messageEvent.getPath().equalsIgnoreCase("/launch_detail_congressional_view"))*/ {
            intent = new Intent(getBaseContext(), DetailCongressionalDisplayActivity.class);
            String name = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            intent.putExtra("name", name);
            intent.putExtra("termEndDate", "5/15/16");
            intent.putExtra("party", "Democrat");

        }

        // fix
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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
