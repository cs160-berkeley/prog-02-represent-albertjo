package com.cs160.joleary.catnip;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class VoteViewActivity extends Activity {

    int zipCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_view);

        //get zip
        this.zipCode = getIntent().getIntExtra("zipCode", 0);
        TextView countyStateTextView = (TextView) findViewById(R.id.countyStateTextView);

        if (this.zipCode == 95014) {
            countyStateTextView.setText("Santa Clara County - CA");
        } else if (this.zipCode == 94704) {
            countyStateTextView.setText("Alameda County - CA");
        }
    }

    public String getCountyForZipCode() {
        return "";
    }
}
