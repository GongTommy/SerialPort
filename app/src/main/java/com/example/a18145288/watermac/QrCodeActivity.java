package com.example.a18145288.watermac;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.a18145288.watermac.adapter.ImagesPagerAdapter;
import com.example.a18145288.watermac.utils.SerialPortUtil;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QrCodeActivity extends Activity {
    private static final String TAG = "QrCodeActivity";
    private SerialPortUtil serialPortUtil;
    private ImageView ivQrCode;
    private ImageView ivNext;
    private ViewPager vpFullAds;
    private Timer fullScrTimer;
    private final int INTERVAL = 300 * 1000;
    /**
     * 没有触摸屏幕的时间
     */
    private int noTouchTime;

//    private Handler fullPicsHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            vpFullAds.setCurrentItem(vpFullAds.getCurrentItem() + 1);
//            fullPicsHandler.sendEmptyMessageDelayed(0, 5000);
//        }
//    };

    private FullAdsHandler fullPicsHandler = new FullAdsHandler(this);
    private static final class FullAdsHandler extends Handler {
        private WeakReference<QrCodeActivity> mActivity;
        public FullAdsHandler(QrCodeActivity activity){
            mActivity = new WeakReference<QrCodeActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity != null){
                QrCodeActivity mainActivity = mActivity.get();
                mainActivity.switchVpFullAds();
            }
        }
    }

    public void switchVpFullAds(){
        vpFullAds.setCurrentItem(vpFullAds.getCurrentItem() + 1);
        fullPicsHandler.sendEmptyMessageDelayed(0, 5000);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_activity);
        initFresco();

        initView();
        initData();
    }

    private void initView() {
        vpFullAds = findViewById(R.id.vpFullAds);
        ivNext = findViewById(R.id.ivNext);
        ivQrCode = findViewById(R.id.ivQrCode);
    }

    private void initData() {
        createFullScreenPics();
        ivNext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        ivNext.setImageResource(R.mipmap.next_pressed);
                        return true;
                    case MotionEvent.ACTION_UP:
                        ivNext.setImageResource(R.mipmap.next_normal);
                        startActivity(new Intent(QrCodeActivity.this, MainActivity.class));
//                        finish();
                        return true;
                }
                return false;
            }
        });
        serialPortUtil = new SerialPortUtil();
        serialPortUtil.openSerialPort();
        serialPortUtil.setOnReceiveComMsg(new SerialPortUtil.OnReceiveComMsg() {
            @Override
            public void receiveComMsg(String msg) {
                //接受串口消息
                if (msg != null){//避免多次跳转
                    if (msg.equals("FC01") || msg.equals("fc01")){
                        startActivity(new Intent(QrCodeActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
    }

    /**
     * 创建全屏轮播页
     */
    private void createFullScreenPics() {
        int pics[] = new int[]{R.mipmap.full_screen_pic3, R.mipmap.full_screen_pic1,
                R.mipmap.full_screen_pic2, R.mipmap.full_screen_pic3, R.mipmap.full_screen_pic1};
        List<SimpleDraweeView> itemViews = new ArrayList<>();
        for(int i = 0; i <pics.length; i++){
            SimpleDraweeView simpleDraweeView = new SimpleDraweeView(QrCodeActivity.this);
            simpleDraweeView.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(pics[i])).build());
            itemViews.add(simpleDraweeView);
            simpleDraweeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vpFullAds.setVisibility(View.GONE);
                    noTouchTime = 0;
                    fullPicsHandler.removeMessages(0);
                }
            });
        }
        vpFullAds.setAdapter(new ImagesPagerAdapter(itemViews, vpFullAds, this));
        vpFullAds.setCurrentItem(1);//要在setAdapter之后设置
        //如果10秒没有点击则展示
        fullScrTimer = new Timer();
        fullScrTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (noTouchTime > INTERVAL){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (vpFullAds.getVisibility() != View.VISIBLE){
                                vpFullAds.setVisibility(View.VISIBLE);
                                noTouchTime = 0;
                                if (fullPicsHandler != null){
                                    fullPicsHandler.sendEmptyMessageDelayed(0, 5000);//实现viewpager的轮播效果。
                                }
                            }
                        }
                    });
                }
                noTouchTime += 1000;
            }
        }, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fullScrTimer != null){
            fullScrTimer.cancel();
            fullScrTimer = null;
        }
        if (fullPicsHandler != null){
            fullPicsHandler.removeMessages(0);
            fullPicsHandler = null;
        }
    }

    /**
     * 初始化fresco，并设置内存策略
     */
    private void initFresco(){
        MemoryTrimmableRegistry memoryTrimmableRegistry = NoOpMemoryTrimmableRegistry.getInstance();
        memoryTrimmableRegistry.registerMemoryTrimmable(new MemoryTrimmable() {
            @Override
            public void trim(MemoryTrimType trimType) {
                final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();

                if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                        || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio) {
                    //清空内存缓存
                    ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
                }
            }
        });
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FrescoCache"))
                .setBaseDirectoryName("rsSystemPicCache").setMaxCacheSize(200 * ByteConstants.MB)
                .setMaxCacheSizeOnLowDiskSpace(100 * ByteConstants.MB)
                .setMaxCacheSizeOnVeryLowDiskSpace(50 * ByteConstants.MB)
                .setMaxCacheSize(80 * ByteConstants.MB).build();

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                .build();

        Fresco.initialize(this, config);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        try {
            if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) { // 60
                ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
            }
        } catch (Exception e) {
            Log.i(TAG, "onTrimMemory");
        }

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
        } catch (Exception e) {
            Log.i(TAG, "onLowMemory");
        }
    }
}
