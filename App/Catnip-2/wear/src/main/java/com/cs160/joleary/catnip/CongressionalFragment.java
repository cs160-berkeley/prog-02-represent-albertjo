package com.cs160.joleary.catnip;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class CongressionalFragment extends CardFragment {
    CongressMember member;

    public CongressionalFragment() {
        super();
    }

    public static CongressionalFragment create(CongressMember member) {
        CongressionalFragment fragment = new CongressionalFragment();
        fragment.addMember(member);
        return fragment;
    }

    public void addMember(CongressMember member) {
        this.member = member;
        Bundle args = new Bundle();
        args.putCharSequence("CardFragment_title", member.name);
        args.putCharSequence("CardFragment_text", member.party);
        this.setArguments(args);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateContentView(inflater, container, savedInstanceState);
        TextView title = (TextView)view.findViewById(android.support.wearable.R.id.title);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getActivity(), WatchToPhoneService.class);
                sendIntent.putExtra("name", member.name);
                getActivity().startService(sendIntent);
            }
        });



        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        System.out.println("action down");
                        return true;
                    }
                }
                return false;
            }

        });
        return view;
    }




}