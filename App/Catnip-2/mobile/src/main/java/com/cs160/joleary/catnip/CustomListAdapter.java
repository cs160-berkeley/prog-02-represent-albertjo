package com.cs160.joleary.catnip;

/**
 * Created by namhyun on 3/1/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<CongressMember> {

    private final Activity context;

    private final CongressMember[] congressMembers;



    public CustomListAdapter(Activity context, CongressMember[] congressMembers) {

        super(context, R.layout.congressional_list_view_cell, congressMembers);
        this.context=context;
        this.congressMembers=congressMembers;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.congressional_list_view_cell, null, true);

        final CongressMember c = congressMembers[position];

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        TextView homePageTextView = (TextView) rowView.findViewById(R.id.homepageTextView);
        TextView emailTextView = (TextView) rowView.findViewById(R.id.emailTextView);
        TextView tweetTextView = (TextView) rowView.findViewById(R.id.tweetTextView);

        if (c.party.equalsIgnoreCase("democrat")) {
            txtTitle.setTextColor(Color.parseColor("#22A7F0"));
        } else {
            txtTitle.setTextColor(Color.parseColor("#F64747"));
        }

        txtTitle.setText(c.name);
        emailTextView.setText(c.email);
        homePageTextView.setText(c.homepageURL);
        tweetTextView.setText(c.tweet);

        Button detailButton = (Button) rowView.findViewById(R.id.detailButton);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DetailCongressionalDisplayActivity.class);
                intent.putExtra("name", c.name);
                intent.putExtra("termEndDate",c.termEndDate);
                intent.putExtra("party", c.party);


                context.startActivity(intent);

            }
        });


        return rowView;
    };


}