package com.mscpz.android.cabinet;

import com.google.gson.JsonObject;
import com.mscpz.android.cabinet.proxy.DefaultProxy;

/**
 * Created by xmx on 2017/8/30.
 */

public interface CabinetProxy {

    void init();

    void openLocker(int boardNo, int lockerNo, ResponseCallback responseCallback);

    public void readState(int boardNo, int lockerNo, ResponseCallback responseCallback);

    interface ResponseCallback {
        void onResponse(JsonObject response);
    }
}
