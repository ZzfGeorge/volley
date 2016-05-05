package com.zhi.volley.image;

import android.support.annotation.NonNull;

import com.zhi.volley.ZhiVolley;

import static com.zhi.volley.image.gif.GifDecoder.BitmapProvider;

public class Image {
    private static final int IMAGE_CACHE_SIZE = 10 * 1024 * 1024; // 5M.

    private static Image sInstance;

    private ImageLoader mImageLoader;
    private ImageDecoder mImageDecoder;
    private BaseImageCache mImageCache;
    private BitmapProvider mBitmapProvider;

    public static Image getInstance() {
        if (sInstance == null) {
            synchronized (Image.class) {
                if (sInstance == null) {
                    sInstance = new Image();
                }
            }
        }
        return sInstance;
    }

    public BitmapProvider getBitmapProvider() {
        if (mBitmapProvider == null) {
            synchronized (BaseBitmapProvider.class) {
                if (mBitmapProvider == null) {
                    mBitmapProvider = new BaseBitmapProvider();
                }
            }
        }
        return mBitmapProvider;
    }

    @NonNull
    public ImageLoader.ImageCache getImageCache() {
        if (mImageCache == null) {
            synchronized (ImageLoader.ImageCache.class) {
                if (mImageCache == null) {
                    mImageCache = new BaseImageCache(IMAGE_CACHE_SIZE);
                }
            }
        }
        return mImageCache;
    }

    @NonNull
    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(ZhiVolley.getInstance().getRequestQueue(), getImageCache());
                }
            }
        }
        return mImageLoader;
    }

    @NonNull
    public ImageDecoder getImageDecoder() {
        if (mImageDecoder == null) {
            synchronized (ImageDecoder.class) {
                if (mImageDecoder == null) {
                    mImageDecoder = new ImageDecoder();
                }
            }
        }
        return mImageDecoder;
    }
}
