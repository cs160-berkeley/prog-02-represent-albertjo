package com.cs160.joleary.catnip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class DetailCongressionalDisplayActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_congressional_display);


        String name, termEndDate, party;

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            name = extras.getString("name");

            termEndDate = extras.getString("termEndDate");
            party = extras.getString("party");

            TextView termEndDateTextView = (TextView) findViewById(R.id.termEndDateTextView);
            termEndDateTextView.setText(termEndDate);

            TextView partyTextView = (TextView) findViewById(R.id.partyTextView);
            partyTextView.setText(party);

        } else {
            name = "error";
        }

        // set color of action bar
        // set Action bar properties (bar color + title + text color)
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#19B5FE")));
        getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>" + name + "</font>"));
    }
}
