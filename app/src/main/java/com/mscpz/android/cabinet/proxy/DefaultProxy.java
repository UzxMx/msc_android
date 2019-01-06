package com.mscpz.android.cabinet.proxy;

import android.util.Log;

import com.google.gson.JsonObject;
import com.mscpz.android.cabinet.CabinetProxy;
import com.mscpz.android.util.HexDumpUtil;
import com.mscpz.android.util.LogManager;
import com.serialport.SerialPort;
import com.serialport.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xmx on 2017/8/30.
 */

public class DefaultProxy implements CabinetProxy {

    private static final String TAG = "CabinetManager";

    private static final int SUCCESS = 0;

    private static final String SUCCESS_STR = "成功";

    private static final int ERRNO_TIMEOUT = 1;

    private static final String ERRNO_TIMEOUT_STR = "超时";

    private static final int ERRNO_OPEN_LOCKER_FAILED = 100;

    private static final String ERRNO_OPEN_LOCKER_FAILED_STR = "开锁失败";

    private static final int ERRNO_READ_STATE_FAILED = 200;

    private static final String ERRNO_READ_STATE_FAILED_STR = "查询状态失败";

    // TODO init device path
    private static final String DEVICE_PATH = "/dev/ttyS0";
//    private static final String DEVICE_PATH = "/dev/ttyS2";

    private SerialPort serialPort;

    private ReadThread readThread;

    private Object readMonitor = new Object();

    private BlockingQueue<Command> commandQueue;

    private CommandQueueExecutor commandQueueExecutor;

    private CommandResult commandResult;

    private Object commandResultMonitor = new Object();

    private static final int COMMAND_RESULT_TIMEOUT_MS = 5 * 1000;

    public DefaultProxy() {
        this.commandQueue = new LinkedBlockingQueue<>();
        this.commandQueueExecutor = new CommandQueueExecutor();
        this.readThread = new ReadThread();
    }

    public void init() {
        openSerialPort();
        commandQueueExecutor.startExecuting();
        readThread.startExecuting();
    }

    private void openSerialPort() {
        if (serialPort != null) {
            closeSerialPort();
        }

        try {
            serialPort = new SerialPort(new File(DEVICE_PATH), 9600, 0);
        } catch (Exception e) {
            LogManager.e(TAG, Log.getStackTraceString(e));
            // TODO upload error to server
        }
    }

    private void closeSerialPort() {
        try {
            serialPort.close();
            serialPort = null;
        } catch (Throwable throwable) {
        }
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

    public void openLocker(int boardNo, int lockerNo, ResponseCallback responseCallback) {
        byte[] cmd = buildOpenLockerCmd(boardNo, lockerNo);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("boardNo", boardNo);
        params.put("lockerNo", lockerNo);
        Command command = new Command(CommandType.OpenLocker, cmd, params, responseCallback);
        this.commandQueue.offer(command);
    }

    public void readState(int boardNo, int lockerNo, ResponseCallback responseCallback) {
        byte[] cmd = buildReadLockerCmd(boardNo, lockerNo);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("boardNo", boardNo);
        params.put("lockerNo", lockerNo);
        Command command = new Command(CommandType.ReadState, cmd, params, responseCallback);
        this.commandQueue.offer(command);
    }

    private void sendData(final byte[] data) {
        trySerialPort(new SerialPortRunnable() {
            @Override
            public void run() throws IOException {
                OutputStream outputStream = serialPort.getOutputStream();
                outputStream.write(data);
                LogManager.d(TAG, "write data success");
            }
        }, 3);
    }

    private class ReadThread extends Thread {

        private boolean running = false;

        private byte[] buffer = new byte[64];

        private boolean readyToRead = false;

        public void startExecuting() {
            this.running = true;
            start();
        }

        public void stopExecuting() {
            this.running = false;
            interrupt();
        }

        public boolean isReadyToRead() {
            return this.readyToRead;
        }

        public void setReadyToRead(boolean readyToRead) {
            this.readyToRead = readyToRead;
        }

        @Override
        public void run() {
            while(running) {
                try {
                    synchronized (readMonitor) {
                        while (!isReadyToRead()) {
                            LogManager.d(TAG, "wait read monitor...");
                            readMonitor.wait();
                        }
                    }

                    LogManager.d(TAG, "start reading...");
                    trySerialPort(new SerialPortRunnable() {
                        @Override
                        public void run() throws IOException {
                            InputStream inputStream = serialPort.getInputStream();
                            while (running && isReadyToRead()) {
                                LogManager.d(TAG, "reading...");
                                int size = inputStream.read(buffer);
                                LogManager.d(TAG, "read return size: " + size);
                                if (size > 0) {
                                    synchronized (commandResultMonitor) {
                                        LogManager.d(TAG, "notify command result");
                                        commandResult = new CommandResult(Arrays.copyOfRange(buffer, 0, size));
                                        commandResultMonitor.notify();
                                        setReadyToRead(false);
                                        break;
                                    }
                                }
                            }
                        }
                    }, 3);
                } catch (Throwable throwable) {
                    LogManager.e(TAG, Log.getStackTraceString(throwable));
                }
            }
        }
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

    public enum CommandType {
        OpenLocker, ReadState
    }

    private static class Command {
        CommandType commandType;
        byte[] cmd;
        Map<String, Object> params;
        ResponseCallback callback;

        public Command(CommandType commandType, byte[] cmd, Map<String, Object> params, ResponseCallback callback) {
            this.commandType = commandType;
            this.cmd = cmd;
            this.params = params;
            this.callback = callback;
        }
    }

    private static class CommandResult {
        byte[] result;

        public CommandResult(byte[] result) {
            this.result = result;
        }
    }

    private class CommandQueueExecutor extends Thread {

        private boolean running = false;

        public void startExecuting() {
            this.running = true;
            start();
        }

        public void stopExecuting() {
            this.running = false;
            this.interrupt();
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    Command command = commandQueue.take();
                    synchronized (commandResultMonitor) {
                        synchronized (readMonitor) {
                            sendData(command.cmd);
                            readThread.setReadyToRead(true);
                            readMonitor.notify();
                        }
                        commandResultMonitor.wait(COMMAND_RESULT_TIMEOUT_MS);
                        readThread.setReadyToRead(false);
                    }

                    deliverResponse(command, commandResult);
                } catch (InterruptedException e) {
                    LogManager.e(TAG, Log.getStackTraceString(e));
                }
            }
        }

        private void deliverResponse(Command command, CommandResult commandResult) {
            if (command.callback == null) {
                return;
            }

            ResponseBuilder builder = new ResponseBuilder();
            if (commandResult == null) {
                builder.error(ERRNO_TIMEOUT, ERRNO_TIMEOUT_STR);
            } else {
                byte[] resultBytes = commandResult.result;
                int boardNo = (int) command.params.get("boardNo");
                int lockerNo = (int) command.params.get("lockerNo");
                if (command.commandType == CommandType.OpenLocker) {
                    if (resultBytes == null || resultBytes.length != 2 || resultBytes[0] != boardNo || resultBytes[1] != 0x59) {
                        builder.error(ERRNO_OPEN_LOCKER_FAILED, ERRNO_OPEN_LOCKER_FAILED_STR);
                    } else {
                        builder.success();
                    }
                } else if (command.commandType == CommandType.ReadState) {
                    if (resultBytes == null || resultBytes.length != 2 || resultBytes[0] != boardNo || (resultBytes[1] != 0 && resultBytes[1] != 1)) {
                        builder.error(ERRNO_READ_STATE_FAILED, ERRNO_READ_STATE_FAILED_STR);
                    } else {
                        builder.success();
                        builder.response.addProperty("state", resultBytes[1]);
                    }
                }
            }

            builder.response.addProperty("return_value", HexDumpUtil.formatByteArray(commandResult == null ? null : commandResult.result));
            command.callback.onResponse(builder.build());
        }
    }

    private static class ResponseBuilder {

        public JsonObject response;

        public ResponseBuilder() {
            this.response = new JsonObject();
        }

        public ResponseBuilder success() {
            response.addProperty("errno", SUCCESS);
            response.addProperty("errmsg", SUCCESS_STR);
            return this;
        }

        public ResponseBuilder error(int errno, String errmsg) {
            response.addProperty("errno", errno);
            response.addProperty("errmsg", errmsg);
            return this;
        }

        public JsonObject build() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("response", this.response);
            return jsonObject;
        }
    }

    private void trySerialPort(SerialPortRunnable runnable, int maxTimes) {
        int i = 0;
        while (i < maxTimes) {
            try {
                runnable.run();
                break;
            } catch (IOException|NullPointerException e) {
                LogManager.e(TAG, "trySerialPort exception: " + Log.getStackTraceString(e));
                i++;
                openSerialPort();
            }
        }
    }

    private interface SerialPortRunnable {
        void run() throws IOException;
    }
}
