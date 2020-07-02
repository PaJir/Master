package com.pajir.master;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.bmob.v3.Bmob;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity_Master";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "5df6f4b28e986d7c9802c9e73bcb61f9");
        Log.d(TAG, "onCreate: init bmob ok");

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
    private void checkCurTime(){
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
        }
    }

    public void startOpenFloating(View view){
        Intent intent = new Intent(this, OpenFloatingService.class);
        // 传参
        Spinner spinnerTime = findViewById(R.id.spinnerTime);
        int chosedTime = Integer.parseInt(spinnerTime.getSelectedItem().toString()) * getResources().getInteger(R.integer.sec_per_min);
        intent.putExtra("chosedTime", chosedTime);
        intent.putExtra("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date()));
        intent.putExtra("objectId", (String)null);
        startActivity(intent);
    }

    public void openRoom(View view){
        startActivity(new Intent(this, OpenRoomActivity.class));
    }

    public void enterRoom(View view){
        startActivity(new Intent(this, EnterRoomActivity.class));
    }
}