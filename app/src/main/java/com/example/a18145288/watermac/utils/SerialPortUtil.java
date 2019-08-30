package com.example.a18145288.watermac.utils;

import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * 串口通信类
 * Created by Administrator on 2019/5/20.
 */

public class SerialPortUtil {
    private static final String TAG = "SerialPortUtil";
    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private ReceiveThread mReceiveThread = null;
    private boolean isStart = false;
    private OnReceiveComMsg onReceiveComMsg;

    /**
     * 打开串口，接收数据
     * 通过串口，接收单片机发送来的数据
     */
    public void openSerialPort() {
        try {
            serialPort = new SerialPort(new File("/dev/ttyS0"), 9600, 0);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            isStart = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        getSerialPort();
    }

    /**
     * 关闭串口
     * 关闭串口中的输入输出流
     */
    public void closeSerialPort() {
        try {
            isStart = false;
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送数据
     * 通过串口，发送数据到单片机
     * 16进制数据
     *
     * @param data 要发送的数据
     */
    public void sendHexSerialPort(String data) {
        if (data == null){
            return;
        }
        try {
            byte[] sendData = DataUtils.HexToByteArr(data);
            outputStream.write(sendData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     * 通过串口，发送数据到单片机
     * 字符串数据
     * @param data 要发送的数据
     */
    public void sendTextSerialPort(String data) {
        if (data == null){
            return;
        }
        try {
            byte[] sendData = DataUtils.textToByteArr(data);
            outputStream.write(sendData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void getSerialPort() {
        if (mReceiveThread == null) {
            mReceiveThread = new ReceiveThread();
        }
        mReceiveThread.start();
    }

    /**
     * 接收串口数据的线程
     */
    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isStart) {
                Log.i(TAG, "Thread Name:" + Thread.currentThread().getName());
                if (inputStream == null) {
                    return;
                }
                byte[] readData = new byte[1024];
                try {
                    int size = inputStream.read(readData);
                    if (size > 0) {
                        String readString = DataUtils.ByteArrToHex(readData, 0, size);
                        Log.i(TAG, "Receiver Msg:" + readString + " Thread Name:" + Thread.currentThread().getName());
                        if (onReceiveComMsg != null){
                            onReceiveComMsg.receiveComMsg(readString);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface OnReceiveComMsg {
        void receiveComMsg(String msg);
    }
    public void setOnReceiveComMsg(OnReceiveComMsg onReceiveComMsg){
        this.onReceiveComMsg = onReceiveComMsg;
    }

}