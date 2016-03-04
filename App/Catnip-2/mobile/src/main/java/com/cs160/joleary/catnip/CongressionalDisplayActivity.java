package com.cs160.joleary.catnip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.ListView;

/**
 * Created by namhyun on 3/1/16.
 */

public class CongressionalDisplayActivity extends Activity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.congressional_display);

        int zipCode = getIntent().getIntExtra("zipCode", 0);

        // set Action bar properties (bar color + title + text color)
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#19B5FE")));
        getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Congress members for "+zipCode+ "</font>"));

        CongressMember c1 = new CongressMember("Rep. Barbara Lee (D)", "Democrat", "lee@house.gov", "www.lee.house.gov", "05/05/18");
        CongressMember c2 = new CongressMember("Sen. Barbara Boxer (D)", "Democrat", "boxer@senate.gov", "www.boxer.senate.gov", "05/05/18");
        CongressMember c3 = new CongressMember("Sen. Dianne Feinstein (D)", "Democrat", "feinstein@senate.gov", "www.feinstein.senate.gov", "05/05/18");
        CongressMember[] congressMembers = new CongressMember[] {c1, c2, c3};

        ListView listView = (ListView)findViewById(R.id.listView);
        CustomListAdapter adapter = new CustomListAdapter(this, congressMembers);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
    }

}
