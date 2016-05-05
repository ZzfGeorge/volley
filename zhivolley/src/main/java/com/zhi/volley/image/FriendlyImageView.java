package com.zhi.volley.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Use animation when switch between image display.
 */
public class FriendlyImageView extends NetworkImageView {
    private static final int DEFAULT_ANIMATION_DURATION_MS = 300;

    private boolean mShouldAnimate = true;
    private int mAnimateDuration = DEFAULT_ANIMATION_DURATION_MS;

    public FriendlyImageView(Context context) {
        super(context);
    }

    public FriendlyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendlyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FriendlyImageView setShouldAnimate(boolean shouldAnimate) {
        mShouldAnimate = shouldAnimate;
        return this;
    }

    public FriendlyImageView setAnimateDuration(int animateDuration) {
        mAnimateDuration = animateDuration;
        return this;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (mShouldAnimate) {
            setAlpha(0f);
            super.setImageDrawable(drawable);
            animate().alpha(1f).setDuration(mAnimateDuration);
        } else {
            super.setImageDrawable(drawable);
        }
    }
}
