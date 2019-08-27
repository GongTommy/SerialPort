package com.example.a18145288.watermac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 18145288 on 2019/6/28.
 */

public class StartPageActivity extends Activity {
    private String TAG = "StartPageActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page_activity);
        new Thread( new Runnable( ) {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i(TAG, "sleep failed");
                    Intent intent = new Intent(StartPageActivity.this, QrCodeActivity.class);
                    startActivity(intent);
                    StartPageActivity.this.finish();
                }
                //耗时任务，比如加载网络数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "sleep normal");
                        Intent intent = new Intent(StartPageActivity.this, QrCodeActivity.class);
                        startActivity(intent);
                        StartPageActivity.this.finish();
                    }
                });
            }
        } ).start();
    }
}
