package com.cs160.joleary.catnip;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class CongressionalFragment extends CardFragment {
    Legislator legislator;
    Drawable image;

    public CongressionalFragment() {
        super();
    }

    public static CongressionalFragment create(Legislator legislator) {
        CongressionalFragment fragment = new CongressionalFragment();
        fragment.addMember(legislator);
        return fragment;
    }

    public void addImage(Drawable image) {
        this.image = image;
    }

    public void addMember(Legislator legislator) {
        this.legislator = legislator;
    }



    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_card_fragment, null);
        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        TextView emailTextView = (TextView) view.findViewById(R.id.emailTextView);
        TextView webTextView = (TextView) view.findViewById(R.id.webTextView);

        nameTextView.setText(legislator.getFullTitleName());
        emailTextView.setText(legislator.email);
        webTextView.setText(legislator.website);


        String colorString;
        switch (legislator.party) {
            case "I":
                colorString="#BF55EC";
                break;
            case "D":
                colorString="#19B5FE";
                break;
            case "R":
                colorString="#F64747";
                break;
            default:
                colorString="#F2784B";
        }

        view.setBackgroundColor(Color.parseColor(colorString));

        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getActivity(), WatchToPhoneService.class);
                System.out.println("id");
                sendIntent.putExtra("id", legislator.id);
                getActivity().startService(sendIntent);
            }
        });

        return view;
    }


    public void setBackgroundImageFromTwiter(View view) {

    }
}