package com.htcy.lightblur;

import android.graphics.Bitmap;
import android.view.View;

/**
 * @author htcy
 * @brief description
 * @date 2021-09-22
 */
public class DefaultSnapshotInterceptor implements ViewBgBlurWrapper.SnapshotInterceptor {

    /**
     * 将View 转 位图
     * @param from
     * @param backgroundColor
     * @param foregroundColor
     * @param scale
     * @param antiAlias
     * @return
     */
    @Override
    public Bitmap snapshot(View from, int backgroundColor, int foregroundColor, float scale, boolean antiAlias) {
        return BitmapProcessor.getInstance().snapshot(from, backgroundColor, foregroundColor, scale, antiAlias);
    }
}
