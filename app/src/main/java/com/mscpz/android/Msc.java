package com.mscpz.android;

import android.content.Context;
import android.os.Handler;
import android.webkit.WebView;

import com.mscpz.android.util.LogManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by xuemingxiang on 16-11-13.
 */

public class Msc {

    private static final String TAG = "XinYa";

    public static final String WX_APP_ID = "wx2391c7e018e08ea8";

    public static final String WX_APP_SECRET = "ab45c179a955a09b5cdf28d0ee25c4ac";

    public static final String TENCENT_APP_ID = "1105463507";

    public static final String TENCENT_APP_SECRET = "zCURv6jxd1yNVX8A";

    public static final String WEIBO_APP_ID = "608690176";

    public static final String WEIBO_APP_SECRET = "5d43c395d3cb75422b51dec5df0fe039";

    public static final int PAGE_SIZE = 10;

    private static Msc singleton;

    private WeakReference<Context> applicationContextWeakRef;

    private Handler handler;

    private String deviceIdForUmeng;

    private boolean debuggable = false;

    private boolean showDashboard = false;

    private Msc() {
    }

    public static Msc getInstance() {
        if (singleton == null) {
            synchronized (Msc.class) {
                if (singleton == null) {
                    singleton = new Msc();
                }
            }
        }
        return singleton;
    }

    public void init(Context applicationContext) {
        LogManager.init(isDebuggable());

        applicationContextWeakRef = new WeakReference<Context>(applicationContext);
        handler = new Handler();
    }

    public boolean isDebuggable() {
        return this.debuggable;
    }

    public boolean isShowDashboard() {
        return this.showDashboard;
    }

    public Context getApplicationContext() {
        if (applicationContextWeakRef != null) {
            return applicationContextWeakRef.get();
        }
        return null;
    }

    public Handler getHandler() {
        return this.handler;
    }
}
