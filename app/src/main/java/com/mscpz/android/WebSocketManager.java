package com.mscpz.android;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;
import com.mscpz.android.cabinet.CabinetManager;
import com.mscpz.android.cabinet.CabinetProxy;
import com.mscpz.android.net.RequestManager;
import com.mscpz.android.util.DeviceUtils;
import com.mscpz.android.util.LogManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xmx on 2017/7/17.
 */

public class WebSocketManager {

    private static final String TAG = "WebSocketManager";

     private static final String URL_CABLE = "ws://mscpz.com/cable";

//    private static final String URL_CABLE = "ws://192.168.1.5:3000/cable";

    private static WebSocketManager singleton;

    private boolean inited = false;

    private Consumer consumer;

    private AdminCabinetChannel adminCabinetChannel;

    private CabinetChannel cabinetChannel;

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

    public void init() {
        if (inited) return;
        doInit();
        inited = true;
    }

    private void doInit() {
        connect();
    }

    private void connect() {
        try {
            URI uri = new URI(URL_CABLE);

            Consumer.Options options = new Consumer.Options();
            options.reconnection = true;
            // TODO what does reconnectionMaxAttempts mean?
            options.reconnectionMaxAttempts = Integer.MAX_VALUE;
            Map<String, String> headers = new HashMap<>();
            headers.put(RequestManager.HEADER_DEVICE_TYPE, "0");
            headers.put(RequestManager.HEADER_DEVICE_ID, DeviceUtils.getDeviceId(Msc.getInstance().getApplicationContext()));
            options.headers = headers;
            consumer = ActionCable.createConsumer(uri, options);
            this.adminCabinetChannel = new AdminCabinetChannel(consumer);
            this.cabinetChannel = new CabinetChannel(consumer);

            consumer.connect();
        } catch (URISyntaxException e) {
            LogManager.e(TAG, Log.getStackTraceString(e));
        }
    }

    private class AdminCabinetChannel {
        private Consumer consumer;

        private Subscription subscription;

        public AdminCabinetChannel(Consumer consumer) {
            this.consumer = consumer;
            this.build();
        }

        private void build() {
            Channel channel = new Channel("Admin::CabinetChannel");
            subscription = consumer.getSubscriptions().create(channel);

            subscription.onConnected(new Subscription.ConnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been successfully completed
                    LogManager.d(TAG, "AdminCabinetChannel onConnected");
                }
            }).onRejected(new Subscription.RejectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription is rejected by the server
                    LogManager.d(TAG, "AdminCabinetChannel onRejected");
                }
            }).onReceived(new Subscription.ReceivedCallback() {
                @Override
                public void call(JsonElement data) {
                    // Called when the subscription receives data from the server
                    LogManager.d(TAG, "AdminCabinetChannel onReceived: " + data.toString());
                    
                    JsonObject jsonObject = data.getAsJsonObject();
                    String action = jsonObject.getAsJsonPrimitive("action").getAsString();
                    final Long userId = jsonObject.getAsJsonPrimitive("user_id").getAsLong();
                    if (action.equals("open")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getProxy().openLocker(boardNo, lockerNo, new CabinetProxy.ResponseCallback() {
                            @Override
                            public void onResponse(JsonObject response) {
                                response.addProperty("user_id", userId);
                                subscription.perform("on_open", response);
                            }
                        });
                    } else if (action.equals("read")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getProxy().readState(boardNo, lockerNo, new CabinetProxy.ResponseCallback() {
                            @Override
                            public void onResponse(JsonObject response) {
                                response.addProperty("user_id", userId);
                                subscription.perform("on_read", response);
                            }
                        });
                    }
//                    sendMsg(userId);
                }
            }).onDisconnected(new Subscription.DisconnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been closed
                    LogManager.d(TAG, "AdminCabinetChannel onDisconnected");
                }
            }).onFailed(new Subscription.FailedCallback() {
                @Override
                public void call(ActionCableException e) {
                    // Called when the subscription encounters any error
                    LogManager.e(TAG, "AdminCabinetChannel onFailed: " + Log.getStackTraceString(e));
                }
            });
        }
    }

    private class CabinetChannel {
        private Consumer consumer;

        private Subscription subscription;

        public CabinetChannel(Consumer consumer) {
            this.consumer = consumer;
            this.build();
        }

        private void build() {
            Channel channel = new Channel("CabinetChannel");
            subscription = consumer.getSubscriptions().create(channel);

            subscription.onConnected(new Subscription.ConnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been successfully completed
                    LogManager.d(TAG, "CabinetChannel onConnected");
                }
            }).onRejected(new Subscription.RejectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription is rejected by the server
                    LogManager.d(TAG, "CabinetChannel onRejected");
                }
            }).onReceived(new Subscription.ReceivedCallback() {
                @Override
                public void call(JsonElement data) {
                    // Called when the subscription receives data from the server
                    LogManager.d(TAG, "CabinetChannel onReceived: " + data.toString());

                    JsonObject jsonObject = data.getAsJsonObject();
                    String action = jsonObject.getAsJsonPrimitive("action").getAsString();
                    if (action.equals("open")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getProxy().openLocker(boardNo, lockerNo, new CabinetProxy.ResponseCallback() {
                            @Override
                            public void onResponse(JsonObject response) {
                                subscription.perform("on_open", response);
                            }
                        });
                    } else if (action.equals("read")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getProxy().readState(boardNo, lockerNo, new CabinetProxy.ResponseCallback() {
                            @Override
                            public void onResponse(JsonObject response) {
                                subscription.perform("on_read", response);
                            }
                        });
                    }
//                    sendMsg(userId);
                }
            }).onDisconnected(new Subscription.DisconnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been closed
                    LogManager.d(TAG, "CabinetChannel onDisconnected");
                }
            }).onFailed(new Subscription.FailedCallback() {
                @Override
                public void call(ActionCableException e) {
                    // Called when the subscription encounters any error
                    LogManager.e(TAG, "CabinetChannel onFailed: " + Log.getStackTraceString(e));
                }
            });
        }
    }
}
