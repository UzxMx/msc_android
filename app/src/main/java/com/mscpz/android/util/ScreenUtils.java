package com.mscpz.android.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by xuemingxiang on 16-11-14.
 */

public class ScreenUtils {

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;
    }

    public static int getScreenWidth(Context context) {
        if (!(context instanceof Activity))
            return 0;

        Activity activity = (Activity) context;
        return getScreenWidth(activity);
    }

    /**
     * 该方法会依据 orientation 来获得正确的x, y
     * portrait时， x 为横向的宽度（一般值较小)
     * landscape时， x 为值大的
     */
    public static Point getScreenSize(Context context) {
        if (!(context instanceof Activity)) {
            return null;
        }
        Point point = new Point();

        Activity activity = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        point.set(dm.widthPixels, dm.heightPixels);
        return point;
    }

    public static Point getRealScreenSize(Activity activity) {
        if (activity == null) {
            return null;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.getWindowManager().getDefaultDisplay().getRealSize(point);
        }
        return point;
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static double getScreenPhysicalDiagonalSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return Math.sqrt(Math.pow(metrics.widthPixels / metrics.xdpi, 2) +
                Math.pow(metrics.heightPixels / metrics.ydpi, 2));
    }

    public static void showStatusBar(Activity activity, boolean show) {
        Window window = activity.getWindow();
        if (show) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }
}
