package com.zhi.volley.image;

import android.support.annotation.NonNull;

import com.zhi.volley.image.gif.GifDecoder;

public class GifModel implements Model {
    @NonNull
    public final GifDecoder decoder;
    public String cacheKey;

    public GifModel(@NonNull GifDecoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public int byteSize() {
        return decoder.getByteSize();
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GifModel gifModel = (GifModel) o;
        return cacheKey != null ? cacheKey.equals(gifModel.cacheKey) : gifModel.cacheKey == null;

    }

    @Override
    public int hashCode() {
        return cacheKey != null ? cacheKey.hashCode() : 0;
    }
}
