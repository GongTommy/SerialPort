package com.example.a18145288.watermac;

import android.app.Application;
import com.example.a18145288.watermac.error.CrashHandler;
import com.example.a18145288.watermac.utils.SerialPortUtil;
import com.example.a18145288.watermac.utils.SterilizeAlarm;

public class WmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        SerialPortUtil.getInstance().openSerialPort();
        SterilizeAlarm.getInstance().init(this);
    }
}
