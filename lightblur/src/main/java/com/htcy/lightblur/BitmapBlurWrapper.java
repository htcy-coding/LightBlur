package com.htcy.lightblur;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

/**
 * @author htcy
 * @brief 单bitmap 高斯模糊
 * @date 2021-09-21
 */
public final class BitmapBlurWrapper {

    private Bitmap mOriginalBitmap = null;

    private  Listener mListener = null;

    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 1;
    private boolean mKeepSize = false;
    private boolean mRecycleOriginal = false;

    private  LightBlur sBlur;

    @SuppressLint("ObsoleteSdkInt")
    public BitmapBlurWrapper(Context context) {
        if (sBlur == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                sBlur = GaussianBlur.getInstance(context);
            } else {
                sBlur = FastBlur.getInstance();
            }
        }
    }



    public BitmapBlurWrapper bitmap(Bitmap original) {
        mOriginalBitmap = original;
        return this;
    }

    public BitmapBlurWrapper staySize(boolean keepSize) {
        this.mKeepSize = keepSize;
        return this;
    }

    public BitmapBlurWrapper recycleOriginal(boolean recycleOriginal) {
        this.mRecycleOriginal = recycleOriginal;
        return this;
    }


    public BitmapBlurWrapper listenBlurTime(Listener listener){
        this.mListener = listener;
        return this;
    }

    public BitmapBlurWrapper scale(float scale) {
        this.mScale = scale;
        return this;
    }

    public BitmapBlurWrapper radius(float radius) {
        this.mRadius = radius;
        return this;
    }

    public Bitmap blur() {
        if (mOriginalBitmap == null) {
            throw new NullPointerException("Bitmap不能为空");
        }
        if (mListener != null) mListener.begin();
        float scale = mScale <= 0 ? 1 : mScale;
        float radius = mPercent <= 0 ? mRadius : Math.min(mOriginalBitmap.getWidth(), mOriginalBitmap.getHeight()) * mPercent;
        Bitmap blurredBitmap = requireBlur().process(mOriginalBitmap, radius, scale, mKeepSize, mRecycleOriginal);
        if (mListener != null) mListener.end();
        return blurredBitmap;
    }

    private  LightBlur requireBlur() {
        return Utils.requireNonNull(sBlur, "Blurred未初始化");
    }


    public void reset() {
        mPercent = 0;
        mRadius = 0;
        mScale = 1;
        mKeepSize = false;
        mRecycleOriginal = false;
    }

    public interface Listener {
        void begin();

        void end();
    }

}
