package com.mscpz.android.util;

import android.util.Log;

/**
 * Created by xuemingxiang on 16-11-11.
 */

public class LogManager {

    public static final int LEVEL_MIN = 0;

    public static final int LEVEL_ERROR = 1;

    public static final int LEVEL_WARN = 2;

    public static final int LEVEL_INFO = 3;

    public static final int LEVEL_DEBUG = 4;

    public static final int LEVEL_MAX = 5;

    private static int currentLogLevel = LEVEL_MIN;

    private LogManager() {
    }

    public static void init(boolean debugEnabled) {
        if (debugEnabled) {
            setCurrentLogLevel(LEVEL_MAX);
        }
    }

    public static void setCurrentLogLevel(int level) {
        currentLogLevel = level;
    }

    public static boolean isErrorEnabled() {
        return currentLogLevel >= LEVEL_ERROR;
    }

    public static boolean isWarnEnabled() {
        return currentLogLevel >= LEVEL_WARN;
    }

    public static boolean isInfoEnabled() {
        return currentLogLevel >= LEVEL_INFO;
    }

    public static boolean isDebugEnabled() {
        return currentLogLevel >= LEVEL_DEBUG;
    }

    public static void e(String tag, String msg) {
        if (isErrorEnabled()) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isWarnEnabled()) {
            Log.w(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isInfoEnabled()) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebugEnabled()) {
            Log.d(tag, msg);
        }
    }

}
