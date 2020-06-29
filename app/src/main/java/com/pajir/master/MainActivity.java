package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Master_MainActivity";
    private final int OVERLAY_PERMISSION_REQ_CODE = 1;
    private final int sec_per_min = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // 目前0是icon，1是label
        View titleView = toolbar.getChildAt(1);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        titleView.setLayoutParams(layoutParams);
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

    // 开启悬浮窗，开启前会检查是否有权限
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

    private void startFloatingWindowService(){
        // 这个是不绑定的方法
        Intent bindIntent = new Intent(this, FloatingWindowService.class);
        // 传参
        Spinner spinnerTime = (Spinner) findViewById(R.id.spinnerTime);
        // 改参数，分变秒，debug时可调小
        int chosedTime = Integer.parseInt(spinnerTime.getSelectedItem().toString()) * sec_per_min;
        bindIntent.putExtra("chosedTime", chosedTime);
        bindIntent.putExtra("startTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        startService(bindIntent);
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
    }

    // 以下与Toolbar有关
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
}