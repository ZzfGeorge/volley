package com.zhi.volley.image;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.android.volley.toolbox.ByteArrayPool;
import com.zhi.volley.image.gif.GifDecoder.BitmapProvider;
import com.zhi.volley.uti.IntArrayPool;

public class BaseBitmapProvider implements BitmapProvider {

    public static final int DEFAULT_POOL_SIZE = 4096;

    private final IntArrayPool mInts;
    private final ByteArrayPool mBytes;

    public BaseBitmapProvider() {
        this(DEFAULT_POOL_SIZE);
    }

    public BaseBitmapProvider(int size) {
        if (size < DEFAULT_POOL_SIZE) {
            size = DEFAULT_POOL_SIZE << 1;
        }
        mBytes = new ByteArrayPool(size);
        mInts = new IntArrayPool(size);
    }

    @NonNull
    @Override
    public Bitmap obtain(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    @Override
    public void release(@NonNull Bitmap bitmap) {
        bitmap.recycle();
    }

    @Override
    public byte[] obtainByteArray(int size) {
        return mBytes.getBuf(size);
    }

    @Override
    public void release(byte[] bytes) {
        mBytes.returnBuf(bytes);
    }

    @Override
    public int[] obtainIntArray(int size) {
        return mInts.getBuf(size);
    }

    @Override
    public void release(int[] array) {
        mInts.returnBuf(array);
    }
}
