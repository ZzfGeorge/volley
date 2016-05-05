package com.zhi.volley;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;

public class ZhiVolley extends Volley {

    private static final ZhiVolley sInstance = new ZhiVolley();

    public static ZhiVolley getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    private RequestQueue mRequestQueue;

    @NonNull
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(String cache, int cacheSize) {
        final HurlStack stack = new HurlStack();
        final Network network = new BasicNetwork(stack);

        final File cacheDir = new File(cache);
        final Cache diskCache;
        if (cacheSize > 0) {
            diskCache = new DiskBasedCache(cacheDir, cacheSize);
        } else {
            diskCache = new DiskBasedCache(cacheDir);
        }

        final RequestQueue queue = new RequestQueue(diskCache, network);
        queue.start();

        return queue;
    }
}
