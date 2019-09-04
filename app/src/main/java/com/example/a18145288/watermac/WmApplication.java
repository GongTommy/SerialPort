package com.example.a18145288.watermac;

import android.app.Application;

import com.example.a18145288.watermac.error.CrashHandler;


public class WmApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
