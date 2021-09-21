package com.htcy.lightblur;

import android.graphics.Bitmap;

/**
 * @author htcy
 * @brief description
 * @date 2021-09-21
 */
public interface LightBlur {


    /**
     * @param originalBitmap
     * @param radius
     * @param scale
     * @param staySize
     * @param recycleOriginal
     * @return
     */
    Bitmap process(final Bitmap originalBitmap,
                   final float radius,
                   final float scale,
                   final boolean staySize,
                   final boolean recycleOriginal);


    /**
     * 回收资源
     */
    void recycle();

}
