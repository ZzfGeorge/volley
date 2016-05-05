package com.zhi.volley.image;

import android.util.LruCache;

public class BaseImageCache implements com.zhi.volley.image.ImageLoader.ImageCache {

    private final LruCache<String, Model> mCache;

    public BaseImageCache(int cacheSize) {

        mCache = new LruCache<String, Model>(cacheSize) {
            @Override
            protected int sizeOf(String key, Model model) {
                return model != null ? model.byteSize() : 0;
            }
        };
    }

    @Override
    public Model getModel(String url) {
        return mCache.get(url);
    }

    @Override
    public void putModel(String url, Model model) {
        mCache.put(url, model);
    }

    public void cleanCache() {
        mCache.evictAll();
    }
}
