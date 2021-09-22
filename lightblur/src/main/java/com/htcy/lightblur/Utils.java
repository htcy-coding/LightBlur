package com.htcy.lightblur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * @author htcy
 * @brief description
 * @date 2021-09-21
 */
public final class Utils {

    static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    static <T> T requireNonNull(T obj) {
        return requireNonNull(obj, "");
    }

    static Bitmap createBitmapFromView(View view, int w, int h) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.clearFocus();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            canvas.setBitmap(null);
        }
        return bitmap;
    }
}
