package com.mscpz.android.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by xmx on 2017/8/5.
 */

public class DeviceUtils {

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
}
