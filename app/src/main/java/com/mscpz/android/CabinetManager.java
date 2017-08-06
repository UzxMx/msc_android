package com.mscpz.android;

import android.util.Log;

import com.mscpz.android.util.LogManager;
import com.serialport.SerialPort;
import com.serialport.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xmx on 2017/7/27.
 */

public class CabinetManager {

    private static final String TAG = "CabinetManager";

    private static CabinetManager singleton;

    private static final String DEVICE_PATH = "/dev/ttymxc2";
//    private static final String DEVICE_PATH = "/dev/ttyS2";

    private SerialPort serialPort;

    private ReadThread readThread;

    public static CabinetManager getInstance() {
        if (singleton == null) {
            synchronized (CabinetManager.class) {
                if (singleton == null) {
                    singleton = new CabinetManager();
                }
            }
        }
        return singleton;
    }

    public void init() throws Exception {
        serialPort = new SerialPort(new File(DEVICE_PATH), 9600, 0);
        readThread = new ReadThread();
        readThread.start();
    }

    public static List<String> getAllDevices() {
        SerialPortFinder finder = new SerialPortFinder();
        String[] devices = finder.getAllDevices();
        List<String> list = new ArrayList<>();
        if (devices != null) {
            for (String device: devices) {
                list.add(device);
            }
        }
        return list;
    }

    public void sendData(byte[] data) {
        OutputStream outputStream = serialPort.getOutputStream();
        try {
            outputStream.write(data);
        } catch (IOException e) {
            LogManager.d(TAG, Log.getStackTraceString(e));
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            InputStream inputStream = serialPort.getInputStream();
            while(!isInterrupted()) {
                LogManager.d(TAG, "reading...");
                int size;
                try {
                    byte[] buffer = new byte[64];

                    if (inputStream == null) return;
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    LogManager.e(TAG, Log.getStackTraceString(e));
                    LogManager.e(TAG, "end reading");
                }
            }
        }
    }

    private void onDataReceived(final byte[] buffer, final int size) {
        LogManager.d(TAG, "onDataReceived: " + " size: " + size);
    }
}
