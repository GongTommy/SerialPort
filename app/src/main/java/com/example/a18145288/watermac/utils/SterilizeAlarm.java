package com.example.a18145288.watermac.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.a18145288.watermac.receiver.SterilizeReceiver;

import java.util.Calendar;

public class SterilizeAlarm {
    private SterilizeAlarm(){

    }
    public static SterilizeAlarm getInstance(){
        return SingleTool.instance;
    }
    private static class SingleTool {
        private static final SterilizeAlarm instance = new SterilizeAlarm();
    }

    public void init(Context mContext){
        Calendar cStart = Calendar.getInstance();
        int hour = cStart.get(Calendar.HOUR_OF_DAY);
        int minute = cStart.get(Calendar.MINUTE);
        int second = cStart.get(Calendar.SECOND);
        if(!(minute == 0 && second == 0)){
            cStart.set(Calendar.HOUR_OF_DAY, hour + 1);
            cStart.set(Calendar.MINUTE, 0);
            cStart.set(Calendar.SECOND, 0);
        }
        Intent intent = new Intent(mContext, SterilizeReceiver.class);
        intent.putExtra("name", "start");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //设置一个PendingIntent对象，发送广播
        AlarmManager amStart = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        amStart.setRepeating(AlarmManager.RTC_WAKEUP, cStart.getTimeInMillis(), 3 * 1000, pi);
    }
}
