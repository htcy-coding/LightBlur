package com.htcy.lightblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;


/**
 * @author htcy
 * @brief description
 * @date 2021-09-21
 */
public final class ViewBgBlurWrapper {

    private static final Float MAX_FPS = 60F;
    private static LightBlur lightBlur;
    private long mLastFrameTime = 0L;


    private float mPercent = 0;
    private float mRadius = 0;
    private float mScale = 1;
    private boolean mAntiAlias = false;
    private boolean mStaySize = false;
    private boolean mFitIntoViewXY = false;
    private boolean mRecycleOriginal = false;
    private float mMaxFps = MAX_FPS;

    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = null;

    private int mBackgroundColor = Color.TRANSPARENT;
    private int mForegroundColor = Color.TRANSPARENT;
    private Bitmap mOriginalBitmap = null;
    private View mViewFrom = null;
    private ImageView mViewInto = null;

    private SnapshotInterceptor mSnapshotInterceptor = null;
    private FpsListener mFpsListener = null;
    private Listener mListener = null;

    public static ViewBgBlurWrapper with(Context context, View view, int w, int h) {
        if (lightBlur == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                lightBlur = GaussianBlur.getInstance(context);
            } else {
                lightBlur = FastBlur.getInstance();
            }
        }
        Bitmap bitmap = Utils.createBitmapFromView(view, w, h);
        return new ViewBgBlurWrapper().bitmap(bitmap, view);
    }

    public ViewBgBlurWrapper bitmap(Bitmap original, View view) {
        mViewFrom = view;
        mOriginalBitmap = original;
        return this;
    }

    public void recycler(){
        lightBlur.recycle();
    }

    public interface SnapshotInterceptor {
        Bitmap snapshot(View from,
                        int backgroundColor,
                        int foregroundColor,
                        float scale,
                        boolean antiAlias);
    }



    public ViewBgBlurWrapper scale(float scale) {
        this.mScale = scale;
        return this;
    }

    public ViewBgBlurWrapper radius(float radius) {
        this.mRadius = radius;
        return this;
    }

    public ViewBgBlurWrapper fpsListener(FpsListener listener) {
        this.mFpsListener = listener;
        return this;
    }

    public ViewBgBlurWrapper listener(Listener listener) {
        this.mListener = listener;
        return this;
    }

    public ViewBgBlurWrapper backgroundColor(int color) {
        mBackgroundColor = color;
        return this;
    }

    public ViewBgBlurWrapper antiAlias(boolean antiAlias) {
        this.mAntiAlias = antiAlias;
        return this;
    }

    public ViewBgBlurWrapper fitIntoViewXY(boolean fit) {
        this.mFitIntoViewXY = fit;
        return this;
    }

    /**
     * ??????????????????????????????
     * viewFrom : ??????with()/view()?????????view
     * viewInto : ?????????ImageView
     * ??????viewFrom???????????????????????????????????????????????????????????????viewInto
     * viewFrom???viewInto???????????????????????????viewInto?????????viewFrom????????????
     */
    public void doBlur(final ImageView into) {
        Utils.requireNonNull(mViewFrom, "??????????????????????????????View????????????");
        Utils.requireNonNull(into, "ImageView????????????");
        mViewInto = into;
        if (mOnPreDrawListener == null) {
            mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mViewInto == null) return true;
                    long currFrameTime = System.currentTimeMillis();
                    final float fps = 1000F / (currFrameTime - mLastFrameTime);
                    if (fps > mMaxFps) return true;
                    mLastFrameTime = currFrameTime;
                    if (mFpsListener != null) mFpsListener.currFps(fps);
                    realTimeMode(true);
                    staySize(false);
                    recycleOriginal(true);
                    Bitmap blur = blur();
                    Bitmap clip = BitmapProcessor.getInstance().clip(blur, mViewFrom, mViewInto, mFitIntoViewXY, mAntiAlias);
                    blur.recycle();
                    mViewInto.setImageBitmap(clip);
                    return true;
                }
            };

            mViewFrom.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
        }
    }

    public ViewBgBlurWrapper staySize(boolean staySize) {
        this.mStaySize = staySize;
        return this;
    }

    public ViewBgBlurWrapper recycleOriginal(boolean recycleOriginal) {
        this.mRecycleOriginal = recycleOriginal;
        return this;
    }

    public static void realTimeMode(boolean realTimeMode) {
        if (lightBlur != null && lightBlur instanceof GaussianBlur) {
            GaussianBlur gaussianBlur = (GaussianBlur) lightBlur;
            gaussianBlur.realTimeMode(realTimeMode);
        }
        BitmapProcessor.getInstance().realTimeMode(realTimeMode);
    }

    public Bitmap blur() {
        if (mViewFrom == null && mOriginalBitmap == null) {
            throw new NullPointerException("?????????View???Bitmap??????????????????");
        }
        if (mListener != null) mListener.begin();
        float scale = mScale <= 0 ? 1 : mScale;
        float radius = mPercent <= 0 ? mRadius : Math.min(
                mViewFrom != null ? mViewFrom.getWidth() : mOriginalBitmap.getWidth(),
                mViewFrom != null ? mViewFrom.getHeight() : mOriginalBitmap.getHeight()
        ) * mPercent;
        final Bitmap blurredBitmap;
        if (radius > 25) {
            scale = scale / (radius / 25);
            radius = 25;
        }
        final SnapshotInterceptor snapshotInterceptor = checkSnapshotInterceptor();
        Bitmap bitmap = snapshotInterceptor.snapshot(mViewFrom, mBackgroundColor, mForegroundColor, scale, mAntiAlias);
        blurredBitmap = lightBlur.process(bitmap, radius, 1, mStaySize, mRecycleOriginal);
        if (mListener != null) mListener.end();
        return blurredBitmap;
    }


    private SnapshotInterceptor checkSnapshotInterceptor() {
        if (mSnapshotInterceptor == null) {
            mSnapshotInterceptor = new DefaultSnapshotInterceptor();
        }
        return mSnapshotInterceptor;
    }

    public interface Listener {
        void begin();

        void end();
    }

    public interface FpsListener {
        void currFps(float fps);
    }
}
