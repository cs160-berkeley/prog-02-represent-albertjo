package com.cs160.joleary.catnip;
/**
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class CircleNetworkImageView extends CircleImageView {
    /** The URL of the network image to load */
    private String mUrl;
    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;
    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;
    /** Local copy of the ImageLoader. */
    private ImageLoader mImageLoader;
    public CircleNetworkImageView(Context context) {
        this(context, null);
    }
    public CircleNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CircleNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     *
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url The URL that should be loaded into this ImageView.
     * @param imageLoader ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary();
    }
    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageResId(int defaultImage) {
        mDefaultImageId = defaultImage;
    }
    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setErrorImageResId(int errorImage) {
        mErrorImageId = errorImage;
    }
    /**
     * Loads the image for the view if it isn't already loaded.
     */
    private void loadImageIfNecessary() {
        int width = getWidth();
        int height = getHeight();
        // if the view's bounds aren't known yet, hold off on loading the image.
        if (width == 0 && height == 0) {
            return;
        }
        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            ImageContainer oldContainer = (ImageContainer) getTag();
            if (oldContainer != null) {
                oldContainer.cancelRequest();
                setImageBitmap(null);
            }
            return;
        }
        ImageContainer oldContainer = (ImageContainer) getTag();
        // if there was an old request in this view, check if it needs to be canceled.
        if (oldContainer != null && oldContainer.getRequestUrl() != null) {
            if (oldContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                oldContainer.cancelRequest();
                setImageBitmap(null);
            }
        }
        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageContainer newContainer = mImageLoader.get(mUrl,
                ImageLoader.getImageListener(this, mDefaultImageId, mErrorImageId));
        // update the tag to be the new bitmap container.
        setTag(newContainer);
        // look at the contents of the new container. if there is a bitmap, load it.
        final Bitmap bitmap = newContainer.getBitmap();
        if (bitmap != null) {
            setImageBitmap(bitmap);
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary();
    }
    @Override
    protected void onDetachedFromWindow() {
        ImageContainer oldContainer = (ImageContainer) getTag();
        if (oldContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            oldContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the tag so we can reload the image if necessary.
            setTag(null);
        }
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}