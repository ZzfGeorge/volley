package com.zhi.volley.image;

import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView.ScaleType;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.zhi.volley.image.gif.GifDecoder;
import com.zhi.volley.image.gif.GifDecoder.BitmapProvider;
import com.zhi.volley.image.gif.GifHeader;
import com.zhi.volley.image.gif.GifHeaderParser;

import java.nio.ByteBuffer;

import static com.android.volley.Response.ErrorListener;
import static com.android.volley.Response.success;

public class ImageGifRequest extends ImageRequest<GifModel> {

    public ImageGifRequest(String url, int maxWidth, int maxHeight, ScaleType scaleType,
            Config decodeConfig, Listener<GifModel> listener, ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
    }

    @Override
    protected Response<GifModel> doParse(NetworkResponse response) {
        final byte[] data = response.data;

        final Options decodeOptions = new Options();
        if (mMaxWidth == 0 && mMaxHeight == 0) {
            decodeOptions.inSampleSize = 1;
        } else {
            // If we have to resize this image, first get the natural bounds.
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;

            // Then compute the dimensions we would ideally like to decode to.
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight, mScaleType);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth, mScaleType);

            // Decode to the nearest power of two scaling factor.
            decodeOptions.inJustDecodeBounds = false;
            // TODO(ficus): Do we need this or is it okay since API 8 doesn't support it?
            // decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
            decodeOptions.inSampleSize =
                    findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        }

        final BitmapProvider provider = Image.getInstance().getBitmapProvider();
        final GifHeaderParser headerParser = new GifHeaderParser().setData(data);
        final GifHeader gifHeader = headerParser.parseHeader();
        final GifDecoder decoder = new GifDecoder(provider, gifHeader, ByteBuffer.wrap(data),
                decodeOptions.inSampleSize);
        final GifModel gifModel = new GifModel(decoder);
        return success(gifModel, HttpHeaderParser.parseCacheHeaders(response));
    }
}
