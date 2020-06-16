package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final int OVERLAY_PERMISSION_REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            Log.d("Master_MainActivity", "FloatingWindowServiceStarted");
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
        int chosedTime = Integer.parseInt(spinnerTime.getSelectedItem().toString()) * 10;
        bindIntent.putExtra("chosedTime", chosedTime);
        startService(bindIntent);
    }

    // 关闭悬浮窗，应该用不到这个
    public void stopFloatingWindowService(View view){
        if(FloatingWindowService.isStarted){
            Log.d("Master_MainActivity", "I will stop service");
            stopService(new Intent(MainActivity.this, FloatingWindowService.class));
        }
    }
}