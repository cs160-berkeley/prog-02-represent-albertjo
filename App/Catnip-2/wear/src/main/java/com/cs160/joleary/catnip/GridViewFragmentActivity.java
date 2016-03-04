package com.cs160.joleary.catnip;

/**
 * Created by namhyun on 3/3/16.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.MotionEvent;
import android.view.View;

public class GridViewFragmentActivity extends FragmentActivity {

    int zipCode = 94704;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        //get zip
        this.zipCode = getIntent().getIntExtra("zipCode", 0);
        System.out.println(this.zipCode);

        final DotsPageIndicator mPageIndicator;
        final GridViewPager mViewPager;

        final String[][] data = {
                { "Row 0, Col 0", "Row 0, Col 1", "Row 0, Col 2" },

        };

        CongressMember c1 = new CongressMember("Rep. Barbara Lee (D)", "Democrat", "lee@house.gov", "www.lee.house.gov", "05/05/18");
        CongressMember c2 = new CongressMember("Sen. Barbara Boxer (D)", "Democrat", "boxer@senate.gov", "www.boxer.senate.gov", "05/05/18");
        CongressMember c3 = new CongressMember("Sen. Dianne Feinstein (D)", "Democrat", "feinstein@senate.gov", "www.feinstein.senate.gov", "05/05/18");


        final CongressMember[][] congressMembers = {{c1,c2,c3}};

        // Get UI references
        mPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        mViewPager = (GridViewPager) findViewById(R.id.pager);

        // Assigns an adapter to provide the content for this pager
        mViewPager.setAdapter(new GridPagerAdapter(getFragmentManager(), congressMembers));
        mPageIndicator.setPager(mViewPager);

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_DOWN):
                        launchVoteViewActivity(v);
                        return true;
                }
                return false;
            }
        });
    }

    public void launchVoteViewActivity(View v) {
        Intent intent = new Intent(this, VoteViewActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("zipCode", this.zipCode);
        startActivity(intent);
    }

    private static final class GridPagerAdapter extends FragmentGridPagerAdapter {

        CongressMember[][] mData;

        private GridPagerAdapter(FragmentManager fm, CongressMember[][] data) {
            super(fm);
            mData = data;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            CongressMember member = mData[row][column];
            CongressionalFragment cardFragment = CongressionalFragment.create(member);

            CardFragment fragment = CardFragment.create(mData[row][column].name,"");

            return cardFragment;
        }



        @Override
        public int getRowCount() {
            return mData.length;
        }

        @Override
        public int getColumnCount(int row) {
            return mData[row].length;
        }
    }


}