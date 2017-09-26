package com.mscpz.android.cabinet.proxy;

import com.google.gson.JsonObject;
import com.mscpz.android.cabinet.CabinetProxy;

/**
 * Created by xmx on 2017/8/30.
 */

public class DebugProxy implements CabinetProxy {

    @Override
    public void init() {
    }

    @Override
    public void openLocker(int boardNo, int lockerNo, ResponseCallback responseCallback) {
        JsonObject response = new JsonObject();
        response.addProperty("errno", 0);
        responseCallback.onResponse(response);
    }

    @Override
    public void readState(int boardNo, int lockerNo, ResponseCallback responseCallback) {
        JsonObject response = new JsonObject();
        response.addProperty("errno", 0);
        response.addProperty("state", 0);
        responseCallback.onResponse(response);
    }
}
