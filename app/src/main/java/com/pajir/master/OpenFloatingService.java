package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpenFloatingService extends AppCompatActivity {
    private static final String TAG = "OpenFloating_Master";
    private final int OVERLAY_PERMISSION_REQ_CODE = 1;

    private static int chosedTime;
    private static String startTime;
    private static String objectId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        chosedTime = intent.getIntExtra("chosedTime", 0);
        startTime = intent.getStringExtra("startTime");
        objectId = intent.getStringExtra("objectId");
        checkAndStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 可以扩展更多的功能...
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Permission allowed", Toast.LENGTH_SHORT).show();
                startFloatingWindowService();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 开启悬浮窗之前，检查是否有权限
    public void checkAndStart() {
        if (FloatingWindowService.isStarted) {
            Log.d(TAG, "FloatingWindowServiceStarted");
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName())), OVERLAY_PERMISSION_REQ_CODE);
        }
        else{
            startFloatingWindowService();
        }
    }

    // 开启悬浮窗前台服务
    private void startFloatingWindowService(){
        Intent intent = new Intent(this, FloatingWindowService.class);
        // 传参
        intent.putExtra("chosedTime", chosedTime);
        intent.putExtra("startTime", startTime);
        intent.putExtra("objectId", objectId);
        Log.d(TAG, "startFloatingWindowService: "+chosedTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
}