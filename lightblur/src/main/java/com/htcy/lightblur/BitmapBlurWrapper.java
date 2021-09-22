package com.htcy.lightblur;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author htcy
 * @brief 单bitmap 高斯模糊
 * @date 2021-09-21
 */
public final class BitmapBlurWrapper {

    private Bitmap mOriginalBitmap = null;
    private Listener mListener = null;
    private Callback mCallback = null;
    private Handler mCallbackHandler = null;

    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 1;
    private boolean mKeepSize = false;
    private boolean mRecycleOriginal = false;

    private LightBlur lightBlur;

    private static ExecutorService sExecutor;

    @SuppressLint("ObsoleteSdkInt")
    public BitmapBlurWrapper(Context context) {
        if (lightBlur == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Utils.requireNonNull(context, "Context为空");
                lightBlur = GaussianBlur.getInstance(context);
            } else {
                lightBlur = FastBlur.getInstance();
            }
        }
    }


    private LightBlur requireBlur() {
        return Utils.requireNonNull(lightBlur, "Blur为空");
    }


    /**异步调用
     * @param callback
     */
    public void doBlur(final Callback callback) {
        Utils.requireNonNull(callback, "Callback不能为空");
        mCallback = callback;
        mCallbackHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                mCallbackHandler = null;
                mCallback.down((Bitmap) msg.obj);
            }
        };
        requireExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = doBlur();
                Message msg = mCallbackHandler.obtainMessage();
                msg.obj = bitmap;
                mCallbackHandler.sendMessage(msg);
            }
        });
    }

    public interface Callback {
        void down(Bitmap bitmap);
    }

    private static ExecutorService requireExecutor() {
        if (sExecutor == null || sExecutor.isShutdown()) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        return sExecutor;
    }

    Bitmap doBlur() {
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

    public void recycler(){
        lightBlur.recycle();
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

    public BitmapBlurWrapper listenBlurTime(BitmapBlurWrapper.Listener listener) {
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


    public interface Listener {
        void begin();

        void end();
    }

}
