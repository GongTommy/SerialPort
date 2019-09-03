package com.example.a18145288.watermac;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.example.a18145288.watermac.adapter.ImagesPagerAdapter;
import com.example.a18145288.watermac.adapter.WatConAdapter;
import com.example.a18145288.watermac.utils.Constants;
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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private List<View> watConViews;
    private ViewPager vpWatCon;
    private Button btnYield1, btnYield2;
    private Button btnHome;
    private WatConAdapter watConAdapter;
    private IjkMediaPlayer mPlayer;
    private SurfaceView surfaceView;
    private ViewPager vpPics;
    private Button btnStart, btnStop;
    private Button barrelSize, bottleSize;
    private SerialPortUtil serialPortUtil;
    private int startTimes = 0;
    private int stopTimes = 0;
    private ViewPager vpFullAds;
    private ImageView ivMoney;
    private Timer fullScrTimer;
    private ImageView ivVideoBg;
    private final int INTERVAL = 5 * 60 * 1000;
    /**
     * 没有触摸屏幕的时间
     */
    private int noTouchTime;


//    private Handler picsHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            vpPics.setCurrentItem(vpPics.getCurrentItem() + 1);
//            picsHandler.sendEmptyMessageDelayed(0, 5000);
//            Log.i(TAG, "CA--->>>2");
//        }
//    };
//
//    private static Handler fullPicsHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            vpFullAds.setCurrentItem(vpFullAds.getCurrentItem() + 1);
//            fullPicsHandler.sendEmptyMessageDelayed(0, 5000);
//            Log.i(TAG, "CA--->>>3");
//        }
//    };

    private PicsHandler picsHandler = new PicsHandler(this);
    private static final class PicsHandler extends Handler {
        private WeakReference<MainActivity> mActivity;
        public PicsHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity != null){
                MainActivity mainActivity = mActivity.get();
                mainActivity.switchVpPics();
            }
        }
    }
    private FullAdsHandler fullPicsHandler = new FullAdsHandler(this);
    private static final class FullAdsHandler extends Handler {
        private WeakReference<MainActivity> mActivity;
        public FullAdsHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity != null){
                MainActivity mainActivity = mActivity.get();
                mainActivity.switchVpFullAds();
            }
        }
    }

    public void switchVpFullAds(){
        vpFullAds.setCurrentItem(vpFullAds.getCurrentItem() + 1);
        fullPicsHandler.sendEmptyMessageDelayed(0, 5000);
    }

    public void switchVpPics(){
        vpPics.setCurrentItem(vpPics.getCurrentItem() + 1);
        picsHandler.sendEmptyMessageDelayed(0, 5000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFresco();
        
        initView();
        initData();
    }

    private void initView() {
        ivVideoBg = findViewById(R.id.ivVideoBg);
        vpFullAds = findViewById(R.id.vpFullAds);
        vpPics = findViewById(R.id.vpPics);
        surfaceView = findViewById(R.id.surfaceView);
//        View view1 = View.inflate(this, R.layout.water_control_view1, null);
//        View view2 = View.inflate(this, R.layout.water_control_view2, null);
//        View view3 = View.inflate(this, R.layout.water_control_view3, null);
        View view4 = View.inflate(this, R.layout.water_control_view4, null);
        watConViews = new ArrayList<>();
//        watConViews.add(view1);
//        watConViews.add(view2);
//        watConViews.add(view3);
        watConViews.add(view4);
        btnHome = view4.findViewById(R.id.btnHome);
        btnStart = view4.findViewById(R.id.btnStart);
        btnStop = view4.findViewById(R.id.btnStop);
//        btnYield1 = view2.findViewById(R.id.btnYield1);
//        btnYield2 = view2.findViewById(R.id.btnYield2);
//        ivMoney = view3.findViewById(R.id.ivMoney);
//        barrelSize = view1.findViewById(R.id.btnBarrel);
//        bottleSize = view1.findViewById(R.id.btnBottle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent");
        noTouchTime = 0;
        return super.onTouchEvent(event);
    }

    private void initData() {
        vpWatCon = findViewById(R.id.vpWatCon);
        watConAdapter = new WatConAdapter(watConViews);
        vpWatCon.setAdapter(watConAdapter);
//        btnYield1.setOnClickListener(this);
//        btnYield2.setOnClickListener(this);
//        ivMoney.setOnClickListener(this);
//        barrelSize.setOnClickListener(this);
//        bottleSize.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        serialPortUtil = new SerialPortUtil();

        createVpPics();
        createFullScreenPics();
        if (surfaceView != null && surfaceView.getHolder() != null){
            surfaceView.getHolder().addCallback(callback);
        }
        createPlayer();
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, QrCodeActivity.class));
                finish();
            }
        });

        serialPortUtil.setOnReceiveComMsg(new SerialPortUtil.OnReceiveComMsg() {
            @Override
            public void receiveComMsg(StringBuilder builder) {
                if (builder == null){
                    return;
                }
                String msg = builder.toString();
                //接受串口消息
                if (msg != null){
                    if(msg.contains("FC02") || msg.contains("fc02")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(getApplication(), "灌装结束", Toast.LENGTH_LONG);
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
                        startActivity(new Intent(MainActivity.this, QrCodeActivity.class));
                        if (serialPortUtil != null){
                            serialPortUtil.closeSerialPort();
                        }
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
            SimpleDraweeView simpleDraweeView = new SimpleDraweeView(MainActivity.this);
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
        //如果10没有点击则展示
        fullScrTimer = new Timer();
        fullScrTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (noTouchTime > INTERVAL){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "CA--->>>1");
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

    /**
     * 创建底部轮播页
     */
    private void createVpPics() {
        int pics[] = new int[]{R.mipmap.pic4, R.mipmap.pic1, R.mipmap.pic2,
                R.mipmap.pic3, R.mipmap.pic4, R.mipmap.pic1};
        List<SimpleDraweeView> itemViews = new ArrayList<>();
        for(int i = 0; i <pics.length; i++){
            SimpleDraweeView simpleDraweeView = new SimpleDraweeView(MainActivity.this);
            simpleDraweeView.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(pics[i])).build());
            itemViews.add(simpleDraweeView);
        }
        vpPics.setAdapter(new ImagesPagerAdapter(itemViews, vpPics, this));
        vpPics.setCurrentItem(1);
        picsHandler.sendEmptyMessageDelayed(0, 5000);//实现viewpager的轮播效果。
    }

    //SurfaceView状态毁掉
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surfaceCreate");
            if (mPlayer != null && surfaceView != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayer.setDisplay(surfaceView.getHolder());
                                if (!mPlayer.isPlaying()){
                                    mPlayer.start();
                                }
                            }
                        });
                    }
                }).start();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surfaceDestroyed");
            if (surfaceView != null) {
                surfaceView.getHolder().removeCallback(callback);
                surfaceView = null;
            }
        }
    };

    private void createPlayer() {
        if (mPlayer == null) {
            mPlayer = new IjkMediaPlayer();
        }
        // 设置倍速，应该是0-2的float类型，可以测试一下
        mPlayer.setSpeed(1.0f);
        // 设置调用prepareAsync不自动播放，即调用start才开始播放
        mPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/sunshine.mp4";
            Log.i(TAG, "path:" + path);
            Uri uri = Uri.parse(path);
            mPlayer.setDataSource(this, uri);
//                mPlayer.setDataSource("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.prepareAsync();
        mPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mPlayer.start();
            }
        });
    }

    //释放播放器
    private void release() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null){
            mPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null && !mPlayer.isPlaying()){
            Log.i(TAG, "onResume1");
            surfaceView = findViewById(R.id.surfaceView);
            surfaceView.getHolder().addCallback(callback);
            mPlayer.setDisplay(surfaceView.getHolder());
            mPlayer.start();
        } else {
            Log.i(TAG, "onResume2");
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ivMoney:
                vpWatCon.setCurrentItem(3);
                break;
            case R.id.btnYield1:
                vpWatCon.setCurrentItem(2);
                String title1 = ((Button)v).getText()!= null ? ((Button)v).getText().toString() : null;
                if (title1 != null){
                    if (title1.equals("7.5L")){
                        sendComOrder(Constants.BUCKET_SMALL_HEX);
                    }else if (title1.equals("150ML")){
                        sendComOrder(Constants.BOTTLE_SMALL_HEX);
                    }
                }
                break;
            case R.id.btnYield2:
                vpWatCon.setCurrentItem(2);
                String title2 = ((Button)v).getText()!= null ? ((Button)v).getText().toString() : null;
                if (title2 != null){
                    if (title2.equals("19.9L")){
                        sendComOrder(Constants.BUCKET_BIG_HEX);
                    }else if (title2.equals("550ML")){
                        sendComOrder(Constants.BOTTLE_BIG_HEX);
                    }
                }
                break;
            case R.id.btnStart:
                sendComOrder(Constants.FILL_WATER_HEX);
                if (serialPortUtil != null){
                    serialPortUtil.openSerialPort();
                }
                break;
            case R.id.btnStop:
                sendComOrder(Constants.PAUSE_WATER_HEX);
                break;
            case R.id.btnBarrel:
                vpWatCon.setCurrentItem(1);
                btnYield1.setText("7.5L");
                btnYield2.setText("18.9L");
                sendComOrder(Constants.BUCKET_SIZE_HEX);
                break;
            case R.id.btnBottle:
                vpWatCon.setCurrentItem(1);
                btnYield1.setText("150ML");
                btnYield2.setText("550ML");
                sendComOrder(Constants.BOTTLE_SIZE_HEX);
                break;
        }
    }

    /**
     * 发送串口命令
     * @param order
     */
    public void sendComOrder(final String order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 5; i++){//规定发送五次，间隔600ms
                    Log.i(TAG, "send com " + i);
//                    serialPortUtil.sendHexSerialPort(order);
                    try {
                        Thread.sleep(100);
                        serialPortUtil.sendHexSerialPort(order);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialPortUtil != null){
            serialPortUtil.closeSerialPort();
        }
        release();
        if (fullScrTimer != null){
            fullScrTimer.cancel();
            fullScrTimer = null;
        }
        if (picsHandler != null){
            picsHandler.removeMessages(0);
            picsHandler = null;
        }
        if (fullPicsHandler != null){
            fullPicsHandler.removeMessages(0);
            fullPicsHandler = null;
        }
    }

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

        }

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
        } catch (Exception e) {

        }
    }


}
