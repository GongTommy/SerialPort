package com.example.a18145288.watermac.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.example.a18145288.watermac.utils.Constants;
import com.example.a18145288.watermac.utils.SerialPortUtil;

public class SterilizeReceiver extends BroadcastReceiver {
    private String TAG = "SterilizeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 5; i++){//规定发送五次，间隔600ms
                    try {
                        Thread.sleep(100);
                        SerialPortUtil.getInstance().sendHexSerialPort(Constants.STERILIZE_START);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 5; i++){//规定发送五次，间隔600ms
                    try {
                        Thread.sleep(100);
                        SerialPortUtil.getInstance().sendHexSerialPort(Constants.STERILIZE_STOP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 3000);
    }
}
