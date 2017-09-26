package com.mscpz.android.cabinet;

import com.mscpz.android.Msc;
import com.mscpz.android.cabinet.proxy.DebugProxy;
import com.mscpz.android.cabinet.proxy.DefaultProxy;

/**
 * Created by xmx on 2017/7/27.
 */

public class CabinetManager {

    private static CabinetProxy proxy;

    public static CabinetProxy getProxy() {
        if (proxy == null) {
            proxy = Msc.getInstance().isDebuggable() ? new DebugProxy() : new DefaultProxy();
        }
        return proxy;
    }
}
