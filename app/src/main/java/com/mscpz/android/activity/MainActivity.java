package com.mscpz.android.activity;

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
import com.mscpz.android.Msc;
import com.mscpz.android.R;
import com.mscpz.android.WebSocketManager;
import com.mscpz.android.cabinet.CabinetManager;
import com.mscpz.android.net.FormStringRequest;
import com.mscpz.android.net.RequestManager;
import com.mscpz.android.net.URL;
import com.mscpz.android.util.DeviceUtils;

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

        View layoutDashboard = findViewById(R.id.layout_dashboard);
        View layoutNonDashboard = findViewById(R.id.layout_non_dashboard);
        boolean showDashboard = Msc.getInstance().isShowDashboard();
        layoutDashboard.setVisibility(showDashboard ? View.VISIBLE : View.GONE);
        layoutNonDashboard.setVisibility(showDashboard ? View.GONE : View.VISIBLE);
        if (showDashboard) {
            //        tvDevices = (TextView) findViewById(R.id.tv_devices);
            tvDeviceId = (TextView) findViewById(R.id.tv_device_id);
//        tvError = (TextView) findViewById(R.id.tv_error);
//
//        etBoardNo = (EditText) findViewById(R.id.et_board_no);
//        etLockerNo = (EditText) findViewById(R.id.et_locker_no);
//        btnOpen = (Button) findViewById(R.id.btn_open);
//        btnReadState = (Button) findViewById(R.id.btn_read_state);
//        tvReturn = (TextView) findViewById(R.id.tv_return);
//        tvCmd = (TextView) findViewById(R.id.tv_cmd);
//
            tvDeviceId.setText(DeviceUtils.getDeviceId(this));
        }

        CabinetManager.getProxy().init();
        WebSocketManager.getInstance().init();
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
}
