package com.htcy.lightblur;

/**
 * @author htcy
 * @brief description
 * @date 2021-09-21
 */
public final class Utils {

    static <T> T requireNonNull(T obj,  String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    static <T> T requireNonNull(T obj) {
        return requireNonNull(obj, "");
    }

}
