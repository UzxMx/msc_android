package com.mscpz.android.net;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mscpz.android.Msc;

/**
 * Created by xuemingxiang on 16-11-13.
 */

public class RequestManager {

    private static final String TAG = "RequestManager";

    public static final String HEADER_DEVICE_TYPE = "MSC-Device-Type";

    public static final String HEADER_DEVICE_ID = "MSC-Device-Id";

    private static RequestManager singleton;

    private RequestQueue requestQueue;

    private RequestManager() {
        requestQueue = Volley.newRequestQueue(Msc.getInstance().getApplicationContext());
    }

    public static RequestManager getInstance() {
        if (singleton == null) {
            synchronized (RequestManager.class) {
                if (singleton == null) {
                    singleton = new RequestManager();
                }
            }
        }
        return singleton;
    }

    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }

    public void addRequest(FormStringRequest request) {
        request.addHeader(HEADER_DEVICE_TYPE, "0");
        request.addHeader(HEADER_DEVICE_ID, "test");
        requestQueue.add(request);
    }
}
