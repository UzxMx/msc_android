package com.mscpz.android;

import android.util.Log;
import android.widget.TextView;

import com.mscpz.android.util.HexDumpUtil;
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

    private static final String TAG = "MainActivity";

    private static CabinetManager singleton;

    private static final String DEVICE_PATH = "/dev/ttymxc3";
//    private static final String DEVICE_PATH = "/dev/ttyS2";

    private SerialPort serialPort;

    private ReadThread readThread;

    private TextView tvReturn;

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

    public void init(TextView tvReturn) throws Exception {
        this.tvReturn = tvReturn;
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

    public void openLocker(int boardNo, int lockerNo) {
        byte[] cmd = buildOpenLockerCmd(boardNo, lockerNo);
        sendData(cmd);
    }

    public void readState(int boardNo, int lockerNo) {
        byte[] cmd = buildReadLockerCmd(boardNo, lockerNo);
        sendData(cmd);
    }

    public void sendData(byte[] data) {
        OutputStream outputStream = serialPort.getOutputStream();
        try {
            outputStream.write(data);
            LogManager.d(TAG, "write success: " + HexDumpUtil.formatHexDump(data, 0, data.length));
        } catch (IOException e) {
            LogManager.d(TAG, Log.getStackTraceString(e));
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                InputStream inputStream = serialPort.getInputStream();
                while(!isInterrupted()) {
                    int size;
                    try {
                        byte[] buffer = new byte[64];

                        if (inputStream == null) return;
                        size = inputStream.read(buffer);
                        LogManager.d(TAG, "reading...");
                        if (size > 0) {
                            onDataReceived(buffer, size);
                        }

                        sleep(500);
                    } catch (Throwable e) {
                        LogManager.e(TAG, Log.getStackTraceString(e));
                    }
                }
            } catch (Throwable throwable) {
                LogManager.e(TAG, Log.getStackTraceString(throwable));
            } finally {
                LogManager.e(TAG, "end reading");
            }
        }
    }

    private void onDataReceived(final byte[] buffer, final int size) {
        tvReturn.post(new Runnable() {
            @Override
            public void run() {
                tvReturn.setText(HexDumpUtil.formatHexDump(buffer, 0, size));
            }
        });
        LogManager.d(TAG, "onDataReceived: " + HexDumpUtil.formatHexDump(buffer, 0, size) + " size: " + size);
    }

    public static byte[] buildOpenLockerCmd(int boardNo, int lockerNo) {
        byte[] cmd = new byte[]{(byte) boardNo, (byte) 0xF2, 0x55, (byte) lockerNo, 0x00, 0x00};
        addCRC(cmd);
        return cmd;
    }

    public static byte[] buildReadLockerCmd(int boardNo, int lockerNo) {
        byte[] cmd = new byte[]{(byte) boardNo, (byte) 0xF1, 0x55, (byte) lockerNo, 0x00, 0x00};
        addCRC(cmd);
        return cmd;
    }

    private static void addCRC(byte[] byteArray) {
        int crc = 0;
        for (int i = 0; i < 4; ++i) {
            crc += (byteArray[i] & 0xff);
        }
        byteArray[4] = (byte) ((crc >> 8) & 0xff);
        byteArray[5] = (byte) (crc & 0xff);
    }
}
