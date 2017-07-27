package com.mscpz.android;

import android.app.Application;

/**
 * Created by xmx on 2017/7/17.
 */

public class MscApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Msc.getInstance().init(this);
    }
}
