package com.mscpz.android;

/**
 * Created by xmx on 2017/7/17.
 */

public class WebSocketManager {

    private static final String TAG = "WebSocketManager";

    private static final String HOST = "192.168.1.20:3000";

    private static final String HTTP_SCHEMA = "http://" + HOST;

    private static final String WS_SCHEMA = "ws://" + HOST;

    private static final String URL_CABLE = WS_SCHEMA + "/cable";

    private static WebSocketManager singleton;

    private boolean inited = false;

//    private WebSocketConnectCallback webSocketConnectCallback = new WebSocketConnectCallback();
//
//    private WebSocketCallback webSocketCallback = new WebSocketCallback();

    private WebSocketManager() {
    }

    public static WebSocketManager getInstance() {
        if (singleton == null) {
            synchronized (WebSocketManager.class) {
                if (singleton == null) {
                    singleton = new WebSocketManager();
                }
            }
        }
        return singleton;
    }

//    public void init() {
//        if (inited) return;
//        doInit();
//        inited = true;
//    }

//    private void doInit() {
//        connect();
//    }
//
//    private void connect() {
//        AsyncHttpGet request = new AsyncHttpGet(URL_CABLE.replace("ws://", "http://").replace("wss://", "https://"));
//        AsyncHttpClient.getDefaultInstance().websocket(request, "", webSocketConnectCallback);
//    }
//
//    private class WebSocketConnectCallback implements AsyncHttpClient.WebSocketConnectCallback {
//
//        @Override
//        public void onCompleted(Exception e, WebSocket webSocket) {
//            Log.d(TAG, "onCompleted");
//
//            if (e != null) {
//                Log.e(TAG, Log.getStackTraceString(e));
//            }
//
//            if (webSocket != null) {
//                webSocket.setStringCallback(webSocketCallback);
//                webSocket.setClosedCallback(new CompletedCallback() {
//                    @Override
//                    public void onCompleted(Exception e) {
//                        Log.d(TAG, "closed");
//                    }
//
//                });
//            }
//        }
//    }
//
//    private class WebSocketCallback implements WebSocket.StringCallback {
//
//        @Override
//        public void onStringAvailable(String s) {
//            Log.d(TAG, "received:" + s);
//        }
//    }
}
