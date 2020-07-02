package com.pajir.master;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FloatingWindowService extends Service {
    private static final String TAG = "Floating_Master";
    public static boolean isStarted = false;
    // unit: second
    private static int chosedTime = 0;
    private static long leftTime = -1;
    private static String startTime;
    private static String endTime;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private TextView textViewCurTime;
    private TextView textViewLeftTime;
    private View floatingView;

    private Handler timeHandle;

    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG, "I am creating");
        //https://yq.aliyun.com/articles/752259 + Android Developers
        Notification notification = createForegroundNoti();
        startForeground(134, notification);
        setParams();
        addReceiver();
    }

    private Notification createForegroundNoti(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String notificationChannelId = "noti_channel_id_01";
        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(this, notificationChannelId)
                        .setContentTitle(getText(R.string.noti_title))
                        .setContentText("")
                        .setSmallIcon(R.drawable.icon)
                        //.setContentIntent(pendingIntent) // 启动内容
                        //.setTicker("???")
                        .build();
    }

    private void setParams(){
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
        //layoutParams.alpha = 0.8f;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void addReceiver(){
        // need to register a broadcast
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: switch");
                switch (intent.getAction()) {
                    case Intent.ACTION_SCREEN_ON:
                        Log.d(TAG, "onReceive: ACTION_SCREEN_ON detected");
                        //timeHandle.removeCallbacksAndMessages(null);
                        fresh();
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                        Log.d(TAG, "onReceive: ACTION_SCREEN_OFF detected");
                        timeHandle.removeCallbacksAndMessages(null);
                        break;
                    default:
                        Log.d(TAG, "onReceive: default");
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    // bounded Bindservice() 这个传递数据
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    // unbounded Startservice() 没数据传递，实现简单
    public int onStartCommand(Intent intent, int flags, int startId){
        isStarted = true;
        // unit: second
        leftTime = chosedTime = intent.getIntExtra("chosedTime", 0);
        startTime = intent.getStringExtra("startTime");
        calDuringTime(chosedTime);
        Log.d(TAG, "get chosedTime & startTime successfully");
        onShowFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: I destroy myself");
        isStarted = false;
        // need to destroy this thread
        timeHandle.removeCallbacksAndMessages(null);
        // need to unregister boardcast receiver
        unregisterReceiver(broadcastReceiver);

        if(windowManager != null)
            windowManager.removeView(floatingView);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplication().startActivity(intent);

        stopForeground(true);
        super.onDestroy();
    }

    // 显示悬浮窗
    private void onShowFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            return;
        }
        // 视图容器
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        floatingView = layoutInflater.inflate(R.layout.service_floating, null);
        /*// 直接关闭服务的按钮，调试用
        Button buttonClose = floatingView.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCurService();
            }
        });
        // */
        Button buttonGiveup = floatingView.findViewById((R.id.buttonGiveup));
        buttonGiveup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // 验证身份
                biometricAuth();
            }
        });

        fresh();

        TextView textViewAllTime = floatingView.findViewById(R.id.textViewAllTime);
        textViewAllTime.setText(String.format(Locale.CHINA, "%d%s", chosedTime / getResources().getInteger(R.integer.sec_per_min), getResources().getString(R.string.minute)));
        Log.d(TAG, "onShowFloatingWindow: "+chosedTime / getResources().getInteger(R.integer.sec_per_min));
        TextView textViewDuringTime = floatingView.findViewById(R.id.textViewDuringTime);
        textViewDuringTime.setText(String.format(Locale.CHINA, "%s - %s", startTime.substring(11, 16), endTime.substring(11, 16)));

        windowManager.addView(floatingView, layoutParams);
    }

    // 这个方法的实际用处是用startTime和offset计算endTime
    private void calDuringTime(int offset){
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(startTime);
        }catch(ParseException e){
            e.printStackTrace();
        }
        Calendar c = new GregorianCalendar();
        c.setTime(startDate);
        c.add(Calendar.SECOND, offset);
        endTime = simpleDateFormat.format(c.getTime());
        writeCurtoDB();
    }

    // return time1 - time2
    private long calTimeDiff(String time1, String time2){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date d1 = null;
        Date d2 = null;
        try{
            d1 = format.parse(time1);
            d2 = format.parse(time2);
        }catch (ParseException e){
            e.printStackTrace();
        }
        long diff = d1.getTime() - d2.getTime();
        return diff / 1000;
    }

    // 这里开线程算时间，也可以直接用DigitalClock, TextClock...
    private void fresh(){
        textViewCurTime = floatingView.findViewById(R.id.textViewCurTime);
        textViewLeftTime = floatingView.findViewById(R.id.textViewLeftTime);
        timeHandle = new Handler(getMainLooper());
        timeHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                textViewCurTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(new Date()));
                String curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
                leftTime = calTimeDiff(endTime, curTime);
                if(leftTime <= 0){
                    writeRecordtoDB();
                    stopSelf();
                }
                else {
                    textViewLeftTime.setText(String.format(Locale.CHINA, "%d", leftTime));
                    //Log.d(TAG, "run: fresh");
                    timeHandle.postDelayed(this, 1000);
                }
                //Log.d("Master_Floating", "I am calculating time");
            }
        }, 10);
    }

    // 服务开启时记录当前信息
    private void writeCurtoDB(){
        Log.d(TAG, "I will write it to db");
        //MasterDBHelper dbHelper = new MasterDBHelper(this);
        MasterDBHelper dbHelper = new MasterDBHelper(this, "master.db", null, 4);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete("CurRecord", "id >= ?", new String[] {"0"});

        ContentValues values = new ContentValues();
        values.put("time_from",startTime);
        values.put("time_end",endTime);
        values.put("time_length", chosedTime / getResources().getInteger(R.integer.sec_per_min));
        db.insert("CurRecord", null, values);
        db.close();
        dbHelper.close();
    }

    // 服务结束时保存并更新信息
    private void writeRecordtoDB(){
        MasterDBHelper dbHelper = new MasterDBHelper(this, "master.db", null, 4);
        Toast.makeText(this, "恭喜完成！你真棒！", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "recerive record finished");
        //MasterDBHelper dbHelper = new MasterDBHelper(context, "master.db", null, 3);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time_from", startTime);
        values.put("time_end", endTime);
        values.put("time_length",chosedTime / getResources().getInteger(R.integer.sec_per_min));
        db.insert("Record", null, values);

        db.delete("CurRecord", "id >= ?", new String[] {"0"});
        db.close();
        dbHelper.close();
    }

    // 拨打电话，弃
    private void sos(){
        // 解绑就能隐藏界面
        windowManager.removeView(floatingView);
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

    private void biometricAuth() {
        Log.d(TAG, "biometricAuth: I will identify you");
        Intent intent = new Intent(FloatingWindowService.this, BiometicAuthority.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getApplication().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "biometricAuth: start activity failed");
        }
    }
}