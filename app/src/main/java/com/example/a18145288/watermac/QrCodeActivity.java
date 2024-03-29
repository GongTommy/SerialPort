package com.example.a18145288.watermac;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private AudioManager audioManager;
    private ImageView ivQrCode;
    private ImageView ivNext;
    private ViewPager vpFullAds;
    private Timer fullScrTimer;
    private int INTERVAL = 2 * 60 * 1000;
    private long START_TIME;
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
                QrCodeActivity qrCodeActivity = mActivity.get();
                qrCodeActivity.switchVpFullAds();
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

    @Override
    protected void onResume() {
        super.onResume();
        START_TIME = System.currentTimeMillis();
        INTERVAL = 10 * 1000;
        muteMusic();
    }

    private void initView() {
        vpFullAds = findViewById(R.id.vpFullAds);
        ivNext = findViewById(R.id.ivNext);
//        ivNext.setVisibility(View.INVISIBLE);
        ivQrCode = findViewById(R.id.ivQrCode);
    }

    private void initData() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
                        finish();
                        return true;
                }
                return false;
            }
        });
        SerialPortUtil.getInstance().setOnReceiveComMsg(new SerialPortUtil.OnReceiveComMsg() {
            @Override
            public void receiveComMsg(StringBuffer builder) {
                Log.i("SerialPortUtil", "Activity1");
                if (builder == null){
                    return;
                }
                String msg = builder.toString();
                //接受串口消息
                if (msg != null){//避免多次跳转
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long currentTime = System.currentTimeMillis();
                            if (ivNext != null && (currentTime - START_TIME > 3000)){
                                ivNext.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    if (msg.contains("FC01") || msg.contains("fc01")){
                        Log.i("SerialPortUtil", "Activity2:" + msg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(getApplication(), "支付成功", Toast.LENGTH_LONG);
                                t.setGravity(Gravity.CENTER, 0, 0);
                                LinearLayout linearLayout = (LinearLayout) t.getView();
                                if (linearLayout != null){
                                    TextView tv = (TextView) linearLayout.getChildAt(0);
                                    if (tv != null){
                                        tv.setTextSize(80);
                                    }
                                }
                                t.show();
                            }
                        });
                        startActivity(new Intent(QrCodeActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
    }

    /**
     * 设置静音，防止灌装页关闭声音失败
     */
    private void muteMusic(){
        if (audioManager != null){
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }
    }

    /**
     * 取消静音
     */
    private void cancelMuteMusic(){
        if (audioManager != null){
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
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
                                INTERVAL = 2 * 60 * 1000;
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
        cancelMuteMusic();
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
