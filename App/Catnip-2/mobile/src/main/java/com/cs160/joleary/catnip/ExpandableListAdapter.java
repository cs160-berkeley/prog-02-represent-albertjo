package com.cs160.joleary.catnip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.cs160.joleary.catnip.Classes.Bill;
import com.cs160.joleary.catnip.Classes.Committee;
import com.cs160.joleary.catnip.Classes.Legislator;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by namhyun on 3/11/16.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Context context;
    private Drawable profileDrawable;
    private String imageURL;

    ArrayList<String> groupNames = new ArrayList<String>() {{
        add("");
        add("Committees");
        add("Bills Sponsored");
    }};

    private Legislator legislator;
    private ArrayList<Committee> committees = new ArrayList<Committee>();
    private ArrayList<Bill> bills = new ArrayList<>();
    private String TWITTER_KEY = "q3HRKhqm8smAlCsweqYoKBHZv";
    private String TWITTER_SECRET = "Bk6s7r5SX33S9P86jBDGZhoudxWbGLnyWKPi8JaHngPf8aDwRb";
    private TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

    public ExpandableListAdapter(Context context, ArrayList<Committee> _committees, ArrayList<Bill> _bills,
                                 Legislator legislator) {
        this.legislator = legislator;
        this.context = context;
        this.committees = _committees;
        this.bills = _bills;

        // image bullshit
        mRequestQueue = Volley.newRequestQueue(context);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });
    }

    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == 0) {
            return 0;
        } else if (groupPosition == 1) {
            return committees.size();
        } else {
            return bills.size();
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition == 0) {
            return null;
        } else if (groupPosition == 1) {
            return committees;
        } else {
            return bills;
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition == 0) {
            return null;
        } else if (groupPosition == 1) {
            return committees.get(childPosition);
        } else {
            return bills.get(childPosition);
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // if group position is either 0
        if (groupPosition == 0) {
            System.out.println(profileDrawable);

            convertView = layoutInflater.inflate(R.layout.info_list_view_header, null);
            TextView termEndDateTextView = (TextView) convertView.findViewById(R.id.termEndDateTextView);
            TextView partyTextView = (TextView) convertView.findViewById(R.id.partyTextView);

            termEndDateTextView.setText(legislator.termEndDate);
            String colorString;
            String party;


            switch (legislator.party) {
                case "I":
                    party = "Independent";
                    colorString="#BF55EC";
                    break;
                case "D":
                    party = "Democrat";
                    colorString="#19B5FE";
                    break;
                case "R":
                    party = "Republican";
                    colorString="#F64747";
                    break;
                default:
                    party = "Unknown";
                    colorString="#F2784B";
            }

            convertView.setBackgroundColor(Color.parseColor(colorString));
            partyTextView.setText(party);
            // update imageView
            if (legislator.twitterID != null && imageURL == null) {
                downloadImageFromTwitterAndUpdateView(convertView);
            } else if (imageURL != null) {
                setProfileDrawable(convertView);
            }

            return convertView;
        }

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_view_header, null);
        }

        int childrenCount;
        if (groupPosition == 1) {
            childrenCount = committees.size();
        } else {
            // if groupPosition = 3
            childrenCount = bills.size();
        }

        TextView groupTextView =  (TextView) convertView.findViewById(R.id.labelTextView);
        TextView countTextView = (TextView) convertView.findViewById(R.id.countTextView);

        groupTextView.setText(groupNames.get(groupPosition));
        countTextView.setText("("+childrenCount+")");

        // keep expanded group expanded
        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition == 1) {
            final Committee committee = (Committee) getChild(groupPosition, childPosition);
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.committees_list_view_cell, null);

            TextView textView =  (TextView) convertView.findViewById(R.id.committee_name);
            textView.setText(committee.name);
            return convertView;

        } else {
            final Bill bill = (Bill) getChild(groupPosition, childPosition);
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.recent_bills_list_view_cell, null);

            TextView billNameTextView =  (TextView) convertView.findViewById(R.id.bill_name_text_view);
            TextView billIDTextView = (TextView) convertView.findViewById(R.id.bill_id_text_view);
            TextView billDateTextView = (TextView) convertView.findViewById(R.id.bill_date_text_view);

            billNameTextView.setText(bill.title);
            billDateTextView.setText(bill.dateIntroduced);
            billIDTextView.setText(bill.billId);
            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    private void downloadImageFromTwitterAndUpdateView(final View view) {
        final String twitterID = legislator.twitterID;
        Fabric.with(context, new Twitter(authConfig));
        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> result) {
                AppSession guestAppSession = result.data;

                new UsersTwitterApiClient(guestAppSession).getUsersService().show(null, twitterID, true,
                        new Callback<User>() {
                            @Override
                            public void success(Result<User> result) {
                                // extract tweet text
                                //String imageURL = result.data.profileImageUrl;
                                imageURL = parseTwitterImageURLForOriginalImage(result.data.profileImageUrl);
                                setProfileDrawable(view);
                            }
                            @Override
                            public void failure(TwitterException exception) {
                            }
                        });
            }
            @Override
            public void failure(TwitterException exception) {
                // unable to get an AppSession with guest auth
                throw exception;
            }
        });
    }

    private String parseTwitterImageURLForOriginalImage(String url) {
        String regexExprMatch = "_[a-z]+?(?=\\.)";
        String newUrl = url.replaceAll(regexExprMatch, "");
        return newUrl;
    }

    private void setProfileDrawable(final View view) {
        final CircleNetworkImageView circleNetworkImageView = (CircleNetworkImageView) view.findViewById(R.id.profile_image);
        circleNetworkImageView.setImageUrl(this.imageURL, this.mImageLoader);
    }


    class UsersTwitterApiClient extends TwitterApiClient {
        public UsersTwitterApiClient(AppSession session) {
            super(session);
        }
        public UsersService getUsersService() {
            return getService(UsersService.class);
        }
    }

    interface UsersService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") Long userId,
                  @Query("screen_name") String screenName,
                  @Query("include_entities") Boolean includeEntities,
                  Callback<User> cb);
    }
}
