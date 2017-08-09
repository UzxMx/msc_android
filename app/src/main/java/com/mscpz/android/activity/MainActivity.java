package com.mscpz.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hosopy.actioncable.ActionCable;
import com.hosopy.actioncable.ActionCableException;
import com.hosopy.actioncable.Channel;
import com.hosopy.actioncable.Consumer;
import com.hosopy.actioncable.Subscription;
import com.mscpz.android.CabinetManager;
import com.mscpz.android.R;
import com.mscpz.android.net.FormStringRequest;
import com.mscpz.android.net.RequestManager;
import com.mscpz.android.net.URL;
import com.mscpz.android.util.DeviceUtils;
import com.mscpz.android.util.HexDumpUtil;
import com.mscpz.android.util.ListUtils;
import com.mscpz.android.util.LogManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xmx on 2017/7/17.
 */

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private TextView tvDevices;

    private TextView tvDeviceId;

    private TextView tvError;

    private Handler handler = new Handler();

    private EditText etBoardNo;

    private EditText etLockerNo;

    private Button btnOpen;

    private Button btnReadState;

    private TextView tvReturn;

    private TextView tvCmd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvDevices = (TextView) findViewById(R.id.tv_devices);
        tvDeviceId = (TextView) findViewById(R.id.tv_device_id);
        tvError = (TextView) findViewById(R.id.tv_error);

        etBoardNo = (EditText) findViewById(R.id.et_board_no);
        etLockerNo = (EditText) findViewById(R.id.et_locker_no);
        btnOpen = (Button) findViewById(R.id.btn_open);
        btnReadState = (Button) findViewById(R.id.btn_read_state);
        tvReturn = (TextView) findViewById(R.id.tv_return);
        tvCmd = (TextView) findViewById(R.id.tv_cmd);

        byte[] cmd = CabinetManager.buildOpenLockerCmd(19, 1);
        LogManager.d(TAG, HexDumpUtil.formatHexDump(cmd, 0, cmd.length));

        try {
            CabinetManager.getInstance().init(tvReturn);
            LogManager.d(TAG, "设备打开成功");
        } catch (Throwable e) {
            LogManager.d(TAG, Log.getStackTraceString(e));
        }
        showDevices();

        LogManager.d(TAG, "deviceId: " + DeviceUtils.getDeviceId(this));
        tvDeviceId.setText(DeviceUtils.getDeviceId(this));

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                LogManager.d(TAG, "send data");
//                CabinetManager.getInstance().sendData(new byte[]{0x13, (byte) 0xF2, 0x55, 0x01, 0x01, 0x5B});
//            }
//        }, 5000);

//        WebSocketManager.getInstance().init();

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int boardNo = Integer.parseInt(etBoardNo.getText().toString());
                int lockerNo = Integer.parseInt(etLockerNo.getText().toString());
                byte[] cmd = CabinetManager.buildOpenLockerCmd(boardNo, lockerNo);
                tvCmd.setText(HexDumpUtil.formatHexDump(cmd, 0, cmd.length));
                CabinetManager.getInstance().openLocker(boardNo, lockerNo);
            }
        });

        btnReadState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int boardNo = Integer.parseInt(etBoardNo.getText().toString());
                int lockerNo = Integer.parseInt(etLockerNo.getText().toString());
                byte[] cmd = CabinetManager.buildReadLockerCmd(boardNo, lockerNo);
                tvCmd.setText(HexDumpUtil.formatHexDump(cmd, 0, cmd.length));
                CabinetManager.getInstance().readState(boardNo, lockerNo);
            }
        });

        try {
            URI uri = new URI("ws://mscpz.com/cable");
//            URI uri = new URI("ws://192.168.1.5:3000/cable");

            Consumer.Options options = new Consumer.Options();
            options.reconnection = true;
            // TODO what does reconnectionMaxAttempts mean?
            options.reconnectionMaxAttempts = Integer.MAX_VALUE;
            Map<String, String> headers = new HashMap<>();
            headers.put(RequestManager.HEADER_DEVICE_TYPE, "0");
            headers.put(RequestManager.HEADER_DEVICE_ID, DeviceUtils.getDeviceId(this));
            options.headers = headers;
            Consumer consumer = ActionCable.createConsumer(uri, options);

            Channel cabinetChannel = new Channel("CabinetChannel");
            final Subscription subscription = consumer.getSubscriptions().create(cabinetChannel);

            subscription.onConnected(new Subscription.ConnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been successfully completed
                    Log.d(TAG, "onConnected");
                }
            }).onRejected(new Subscription.RejectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription is rejected by the server
                    Log.d(TAG, "onRejected");
                }
            }).onReceived(new Subscription.ReceivedCallback() {
                @Override
                public void call(JsonElement data) {
                    // Called when the subscription receives data from the server
                    Log.d(TAG, "onReceived: " + data.toString());
                    JsonObject jsonObject = data.getAsJsonObject();
                    String action = jsonObject.getAsJsonPrimitive("action").getAsString();
                    if (action.equals("open")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getInstance().openLocker(boardNo, lockerNo);
                    } else if (action.equals("read")) {
                        int boardNo = jsonObject.getAsJsonPrimitive("board_no").getAsInt();
                        int lockerNo = jsonObject.getAsJsonPrimitive("locker_no").getAsInt();
                        CabinetManager.getInstance().readState(boardNo, lockerNo);
                    }
//                    sendMsg(userId);
                }
            }).onDisconnected(new Subscription.DisconnectedCallback() {
                @Override
                public void call() {
                    // Called when the subscription has been closed
                    Log.d(TAG, "onDisconnected");
                }
            }).onFailed(new Subscription.FailedCallback() {
                @Override
                public void call(ActionCableException e) {
                    // Called when the subscription encounters any error
                    Log.d(TAG, "onFailed: " + Log.getStackTraceString(e));
                }
            });

            consumer.connect();

//            subscription.perform("away");
//
//// 5. Perform any action using JsonObject(GSON)
//            JsonObject params = new JsonObject();
//            params.addProperty("foo", "bar");
//            subscription.perform("appear", params);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(Long userId) {
        FormStringRequest request = new FormStringRequest(Request.Method.POST, URL.URL_SEND_MSG, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "sendMsg onResponse: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "sendMsg onErrorResponse: " + Log.getStackTraceString(error));
            }
        });
        request.addParam("user_id", userId.toString());
        RequestManager.getInstance().addRequest(request);
    }

    private void showDevices() {
        List<String> devices = CabinetManager.getAllDevices();
        tvDevices.setText(ListUtils.toString(devices));
    }
}
