package com.cs160.joleary.catnip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by namhyun on 3/12/16.
 */
public class SendImageToWearable  {
    private Context context;
    private GoogleApiClient mApiClient;
    private HashMap<String, String> urlHashMap;

    public SendImageToWearable(Context context, HashMap<String, String> urlHashMap) {
        this.context = context;
        this.urlHashMap = urlHashMap;

        mApiClient = new GoogleApiClient.Builder(context)
                .addApi( Wearable.API )
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        sendToWearable();
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        System.out.println(cause);
                    }
                })
                .build();
        mApiClient.connect();
    }

    private void sendToWearable() {
        new Thread( new Runnable() {
            @Override
            public void run() {

                for (String id : urlHashMap.keySet()) {
                    String url = urlHashMap.get(id);

                    Drawable drawable = getDrawableFromURL(url);
                    if (drawable != null) {
                        // Get asset from drawable
                        Asset asset = createAssetFromDrawable(drawable);
                        System.out.println("adding now for id:" + id);

                        PutDataRequest request = PutDataRequest.create("/image");
                        request.putAsset("image"+id, asset);
                        Wearable.DataApi.putDataItem(mApiClient, request);

                    }
                }


            }
        }).start();
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
}
