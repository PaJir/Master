package com.pajir.master;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class FloatingWindowService extends Service {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private Button button;
    private TextView textViewCurTime;
    private View floatingView;

    @Override
    public void onCreate(){
        super.onCreate();
        isStarted = true;
        Log.d("Master_Floating", "I am creating");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        // 窗口类型，兼容一下老版本
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // application一般是单例
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        else{
            // 电话窗口
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 像素点格式
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        // 窗口的行为准则
        // FLAG_NOT_TOUCH_MODAL: 不监听窗口之外的按键，这里可有可无
        // FLAG_LAYOUT_IN_SCREEN: 允许窗口占满整个屏幕
        // FLAG_LAYOUT_NOT_FOCUSABLE: 不接受任何按键或按钮事件
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        // 窗口的透明度，用于debug
        layoutParams.alpha = 0.8f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    // bounded Bindservice() 这个传递数据
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    // unbounded Startservice() 没数据传递
    public int onStartCommand(Intent intent, int flags, int startId){
        onShowFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        isStarted = false;
        super.onDestroy();
        Log.d("Master_Floating", "I destroy myself");
    }

    private void onShowFloatingWindow(){
        if(Settings.canDrawOverlays(this)){
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            floatingView = layoutInflater.inflate(R.layout.service_floating, null);
            // 直接关闭服务的按钮，调试用
            button = floatingView.findViewById(R.id.buttonClose);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    windowManager.removeView(floatingView);
                    stopSelf();
                }
            });

            textViewCurTime = floatingView.findViewById(R.id.textViewCurTime);
            final Handler aHandle = new Handler(getMainLooper());
            aHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textViewCurTime.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    aHandle.postDelayed(this, 1000);
                }
             }, 10);

            windowManager.addView(floatingView, layoutParams);
        }
    }

}
