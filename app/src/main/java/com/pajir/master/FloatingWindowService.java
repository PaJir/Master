package com.pajir.master;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.icu.text.AlphabeticIndex;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FloatingWindowService extends Service {
    private final String TAG = "Master_Floating";
    public static boolean isStarted = false;
    // unit: second
    private static int chosedTime = 0;
    private static int leftTime = -1;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private TextView textViewCurTime;
    private TextView textViewLeftTime;
    private View floatingView;

    private Handler timeHandle;

    private RecordReceiver recordReceiver;

    @Override
    public void onCreate(){
        super.onCreate();
        isStarted = true;
        Log.d(TAG, "I am creating");
        if(windowManager == null)
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if(layoutParams == null)
            layoutParams = new WindowManager.LayoutParams();
        // 窗口类型，兼容一下老版本
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // application一般是单例
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        else{
            // 老版本Android用电话窗口类型
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 像素点格式
        layoutParams.format = PixelFormat.RGBA_8888;
        //layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 窗口的行为准则
        // FLAG_NOT_TOUCH_MODAL: 不监听窗口之外的按键，这里可有可无
        // FLAG_LAYOUT_IN_SCREEN: 允许窗口占满整个屏幕
        // FLAG_LAYOUT_NOT_FOCUSABLE: 不接受任何按键或按钮事件
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // 窗口的透明度，用于debug
        layoutParams.alpha = 0.8f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        // need to register a broadcast
        recordReceiver = new RecordReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RECORDFINISHED");
        registerReceiver(recordReceiver, intentFilter);
    }

    @Override
    // bounded Bindservice() 这个传递数据
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    // unbounded Startservice() 没数据传递，实现简单
    public int onStartCommand(Intent intent, int flags, int startId){
        leftTime = chosedTime = intent.getIntExtra("chosedTime", 0);
        Log.d(TAG, "get chosedTime successfully");
        onShowFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void stopCurService(){
        if(windowManager != null)
            windowManager.removeView(floatingView);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        isStarted = false;
        // need to destroy this thread
        timeHandle.removeCallbacksAndMessages(null);
        // need to unregister boardcast receiver
        unregisterReceiver(recordReceiver);
        super.onDestroy();
        Log.d(TAG, "I destroy myself");
    }

    private void onShowFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            return;
        }
        // 视图容器
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        floatingView = layoutInflater.inflate(R.layout.service_floating, null);
        // 直接关闭服务的按钮，调试用
        Button button = floatingView.findViewById(R.id.buttonClose);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCurService();
            }
        });

        // 这里开线程算时间，也可以直接用DigitalClock, TextClock...
        textViewCurTime = floatingView.findViewById(R.id.textViewCurTime);
        textViewLeftTime = floatingView.findViewById(R.id.textViewLeftTime);
        timeHandle = new Handler(getMainLooper());
        timeHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(leftTime == 0){
                    Intent intent = new Intent();
                    intent.setAction("RECORDFINISHED");
                    sendBroadcast(intent);
                    stopCurService();
                    return;
                }
                textViewCurTime.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                leftTime -= 1;
                textViewLeftTime.setText(Integer.toString(leftTime) + "/" + Integer.toString(chosedTime));
                timeHandle.postDelayed(this, 1000);
                //Log.d("Master_Floating", "I am calculating time");
            }
        }, 10);

        TextView textViewAllTime = floatingView.findViewById(R.id.textViewAllTime);
        textViewAllTime.setText(Integer.toString(chosedTime / 60) + "min");

        TextView textViewDuringTime = floatingView.findViewById(R.id.textViewDuringTime);
        textViewDuringTime.setText("Master Time: " + calDuringTime("HH:mm", chosedTime));

        Button button1 = floatingView.findViewById(R.id.buttonSOS);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Toast.makeText(FloatingWindowService.this, "Unavailable Now", Toast.LENGTH_SHORT).show();
                //sos();
            }
        });

        windowManager.addView(floatingView, layoutParams);
    }

    @NonNull
    private String calDuringTime(String format, int offset){
        String startTime = new SimpleDateFormat(format).format(new Date());
        SimpleDateFormat endDateFormat = new SimpleDateFormat(format);
        Calendar c = new GregorianCalendar();
        c.add(Calendar.SECOND, offset);
        String endTime = endDateFormat.format(c.getTime());
        return startTime + " - " + endTime;
    }

    private void sos(){
        // 解绑就能隐藏界面
        //windowManager.removeView(floatingView);
        // 然后打开拨号界面
        Log.d(TAG, "I will sos");
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+"110"));
        // 一定要加这个flag，否则无法service -> activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getApplication().startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG, "You(SERVICE) can't open ACTIVITY without adding flag!");
        }
        // 怎么再显示回来啊
        //windowManager.addView(floatingView, layoutParams);

    }
}