package com.zhi.volley.image;

import android.graphics.Bitmap;

public class GifFrameModel implements Model {

    public Bitmap bitmap;

    @Override
    public int byteSize() {
        if (bitmap != null && !bitmap.isRecycled()) {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
        return 0;
    }

    @Override
    public boolean check() {
        return false;
    }
}
