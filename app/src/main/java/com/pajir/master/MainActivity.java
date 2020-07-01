package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity_Master";
    private final int OVERLAY_PERMISSION_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        checkCurTime();
    }

    // 以下三个与Toolbar有关
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // 目前0是icon，1是label
        View titleView = toolbar.getChildAt(1);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        titleView.setLayoutParams(layoutParams);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.history:
                // 查看历史记录
                Intent intent1 = new Intent(this, History.class);
                startActivity(intent1);
                return true;
            case R.id.about:
                // 关于
                Intent intent2 = new Intent(this, About.class);
                startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 检查当前是否正处于Master time 中
    private boolean checkCurTime(){
        Log.d(TAG, "checkCurTime: whether it is in Master time");
        MasterDBHelper dbHelper = new MasterDBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String time_from = null, time_end = null;
        int time_length = 0;
        Cursor cursor = db.query("CurRecord", null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            time_from = cursor.getString(cursor.getColumnIndexOrThrow("time_from"));
            time_end = cursor.getString(cursor.getColumnIndexOrThrow("time_end"));
            time_length = cursor.getInt(cursor.getColumnIndexOrThrow("time_length"));
        }
        cursor.close();
        db.close();
        dbHelper.close();
        Log.d(TAG, String.format(Locale.CHINA,"checkCurTime: %s, %s, %d", time_from, time_end, time_length));
        if(time_from != null) {
            Intent intent = new Intent(this, FloatingWindowService.class);
            intent.putExtra("chosedTime", time_length * getResources().getInteger(R.integer.sec_per_min));
            intent.putExtra("startTime", time_from);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 可以扩展更多的功能...
        switch(requestCode){
            case OVERLAY_PERMISSION_REQ_CODE:
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show();
                    startFloatingWindowService();
                    }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 开启悬浮窗之前，检查是否有权限
    public void startFloatingWindow(View view) {
        if (FloatingWindowService.isStarted) {
            Log.d(TAG, "FloatingWindowServiceStarted");
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName())), OVERLAY_PERMISSION_REQ_CODE);
        }
        else{
            startFloatingWindowService();
        }
    }

    // 开启悬浮窗前台服务
    private void startFloatingWindowService(){
        // 这一步可能没必要，不过防止出现意外（没发现的bug）
        if(checkCurTime()){
            return;
        }
        // 这个是不绑定的方法
        Intent intent = new Intent(this, FloatingWindowService.class);
        // 传参
        Spinner spinnerTime = (Spinner) findViewById(R.id.spinnerTime);
        int chosedTime = Integer.parseInt(spinnerTime.getSelectedItem().toString()) * getResources().getInteger(R.integer.sec_per_min);
        intent.putExtra("chosedTime", chosedTime);
        intent.putExtra("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date()));
        Log.d(TAG, "startFloatingWindowService: "+chosedTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    // 关闭悬浮窗，应该用不到这个
    public void stopFloatingWindowService(View view){
        if(FloatingWindowService.isStarted){
            Log.d(TAG, "I will stop service");
            stopService(new Intent(MainActivity.this, FloatingWindowService.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: I destroy myself");
    }
}