package com.cs160.joleary.catnip;

/**
 * Created by namhyun on 3/1/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.cs160.joleary.catnip.Classes.Legislator;
import com.google.android.gms.wearable.Asset;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

public class CustomListAdapter extends ArrayAdapter<Legislator> {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private final Activity context;
    private final List<Legislator> legislators;
    public AppSession guestAppSession;
    private HashMap<String, String> urlHashMap = new HashMap<>();

    public CustomListAdapter(Activity context,  List<Legislator> legislators) {
        super(context, R.layout.congressional_list_view_cell, legislators);
        this.context=context;
        this.legislators=legislators;

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

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.congressional_list_view_cell, null, true);

        final Legislator legislator = legislators.get(position);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        TextView homePageTextView = (TextView) rowView.findViewById(R.id.homepageTextView);
        TextView emailTextView = (TextView) rowView.findViewById(R.id.emailTextView);

        txtTitle.setText(legislator.getFullTitleName());
        emailTextView.setText(legislator.email);
        homePageTextView.setText(legislator.website);

        // update Tweet view if twittter ID is not null
        if (legislator.twitterID != null) {
            updateTweetViewAndImageView(rowView, legislator.twitterID, legislator.id);
        } else {
            // if no twitter account, tweet view
            ImageView twitterLogoView = (ImageView) rowView.findViewById(R.id.twitterLogo);
            //twitterLogoView.getLayoutParams().height = 0;
            twitterLogoView.setImageDrawable(null);
            rowView.findViewById(R.id.tweetView).setVisibility(View.GONE);

            // send null image

        }

        ImageButton detailViewButton = (ImageButton) rowView.findViewById(R.id.detailViewButton);
        detailViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailCongressionalDisplayActivity.class);
                intent.putExtra("legislator", legislator);
                context.startActivity(intent);
            }
        });
        return rowView;
    }

    private void updateTweetViewAndImageView(View rowView, String twitterId, final String legislatorId) {
        final TextView tweetTextView = (TextView) rowView.findViewById(R.id.tweetTextView);
        final CircleNetworkImageView imageView = (CircleNetworkImageView) rowView.findViewById(R.id.iconImageView);

        new UsersTwitterApiClient(guestAppSession).getUsersService().show(null, twitterId, true,
                new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        // extract tweet text
                        String tweet = result.data.status.text;

                        String imageURL = parseTwitterImageURLForOriginalImage(result.data.profileImageUrl);

                        tweetTextView.setText(tweet);

                        //extract profile photo
                        imageView.setImageUrl(imageURL, mImageLoader);

                        // transmit image to wearable
                        urlHashMap.put(legislatorId, imageURL);
                        if (urlHashMap.keySet().size() == legislators.size()) {
                            new SendImageToWearable(context, urlHashMap);
                        }

                    }

                    @Override
                    public void failure(TwitterException exception) {
                    }
                });
    }

    private static Asset createAssetFromDrawable(Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private Drawable getDrawableFromURL(final String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private String parseTwitterImageURLForOriginalImage(String url) {
        String regexExprMatch = "_[a-z]+?(?=\\.)";
        String newUrl = url.replaceAll(regexExprMatch, "");
        return newUrl;
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