package com.pajir.master;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class FloatingWindowService extends Service {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private Button button;

    @Override
    public void onCreate(){
        super.onCreate();
        isStarted = true;
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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // 窗口的透明度，用于debug
        layoutParams.alpha = 0.5f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        //layoutParams.x = 300;
        //layoutParams.y = 300;
    }

    @Override
    // bounded Bindservice()
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    // unbounded Startservice()
    public int onStartCommand(Intent intent, int flags, int startId){
        onShowFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void onShowFloatingWindow(){
        if(Settings.canDrawOverlays(this)){
            button = new Button(getApplicationContext());

            button.setText("Floating Window");
            button.setBackgroundColor(Color.WHITE);
            windowManager.addView(button, layoutParams);
            button.setOnTouchListener(new FloatingOnTouchListener());
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
