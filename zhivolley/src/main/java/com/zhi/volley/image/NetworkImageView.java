/**
 * Copyright (C) 2013 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhi.volley.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.zhi.volley.image.ImageLoader.ImageListener;

/**
 * Handles fetching an image from a URL as well as the life-cycle of the
 * associated request.
 */
public class NetworkImageView extends ImageView {
    /** The URL of the network image to load */
    private String mUrl;
    private int mType;
    private Resources mImageRes;

    /**
     * Resource ID of the image to be used as a placeholder until the network image is loaded.
     */
    private int mDefaultImageId;

    /**
     * Resource ID of the image to be used if the network response fails.
     */
    private int mErrorImageId;

    /** Current ImageContainer. (either in-flight or finished) */
    private ImageLoader.ImageContainer mImageContainer;

    private ImageDecoder.DecodeContainer mDecodeContainer;

    public NetworkImageView(Context context) {
        this(context, null);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
     * <p/>
     * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)} and
     * {@link NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url The URL that should be loaded into this ImageView.
     */
    public void setImageUrl(String url) {
        cancelDecode();

        mType = Model.MODEL_BITMAP;
        mUrl = url;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    public void setGifUrl(String url) {
        if (mType != Model.MODEL_GIF) {
            cancelRequest();
        }

        mType = Model.MODEL_GIF;
        mUrl = url;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    public void setImageResources(Resources customRes) {
        mImageRes = customRes;
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
     *
     * @param isInLayoutPass True if this was invoked from a layout pass, false otherwise.
     */
    void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();
        ScaleType scaleType = getScaleType();

        boolean wrapWidth = false;
        boolean wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
            wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
        }

        // if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
        // view, hold off on loading the image.
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.url != null) {
            if (mImageContainer.url.equals(mUrl)) {
                // if the request is from the same URL, return.
                if (mImageContainer.model instanceof GifModel) {
                    setImageGifModel((GifModel) mImageContainer.model);
                }
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        // Calculate the max image width / height to use while ignoring WRAP_CONTENT dimens.
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = Image.getInstance().getImageLoader().get(mUrl, new ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setErrorImageOrNull();
            }

            @Override
            public void onResponse(@NonNull final ImageLoader.ImageContainer response, boolean isImmediate) {
                // If this was an immediate response that was delivered inside of a layout
                // pass do not set the image immediately as it will trigger a requestLayout
                // inside of a layout. Instead, defer setting the image by posting back to
                // the main thread.
                if (isImmediate && isInLayoutPass) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onResponse(response, false);
                        }
                    });
                    return;
                }
                setImageModel(response.model);
            }
        }, maxWidth, maxHeight, scaleType, mType);
    }

    protected void setDefaultImageOrNull() {
        if (mDefaultImageId != 0) {
            if (mImageRes != null) {
                setImageDrawable(mImageRes.getDrawable(mDefaultImageId));
            } else {
                setImageResource(mDefaultImageId);
            }
        } else {
            setImageBitmap(null);
        }
    }

    protected void setErrorImageOrNull() {
        if (mErrorImageId != 0) {
            if (mImageRes != null) {
                setImageDrawable(mImageRes.getDrawable(mErrorImageId));
            } else {
                setImageResource(mErrorImageId);
            }
        } else {
            setDefaultImageOrNull();
        }
    }

    protected void setImageModel(Model model) {
        if (model instanceof BitmapModel) {
            setImageBitmapModel((BitmapModel) model);
        } else if (model instanceof GifModel) {
            setImageGifModel((GifModel) model);
        } else {
            setDefaultImageOrNull();
        }
    }

    protected void setImageBitmapModel(@NonNull BitmapModel model) {
        if (model.check()) {
            setImageBitmap(model.bitmap);
        } else {
            setDefaultImageOrNull();
        }
    }

    protected void setImageGifModel(GifModel model) {
        if (model == null || !model.check()) {
            if (mDecodeContainer != null) {
                mDecodeContainer.cancelRequest();
                mDecodeContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }
        // if there was an old decode in this view, check if it needs to be canceled.
        if (mDecodeContainer != null) {
            if (mDecodeContainer.model.equals(model)) {
                if (!mDecodeContainer.visible) {
                    mDecodeContainer.resumeRequest();
                }
                return;
            } else {
                mDecodeContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        mDecodeContainer = Image.getInstance().getImageDecoder().get(model, new ImageDecoder.DecodeListener() {
            @Override
            public void onResponse(@NonNull Bitmap bitmap, int frame, int count) {
                // Here, first set is permit.
                mDecodeContainer.visible = isShown();
                setImageBitmap(bitmap);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelRequest();
        cancelDecode();
        super.onDetachedFromWindow();
    }

    private void cancelRequest() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }

    }

    private void cancelDecode() {
        if (mDecodeContainer != null) {
            mDecodeContainer.cancelRequest();
            mDecodeContainer = null;
            setImageBitmap(null);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
