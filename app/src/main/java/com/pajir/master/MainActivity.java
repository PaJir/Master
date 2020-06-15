package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
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
                    startService(new Intent(MainActivity.this, FloatingWindowService.class));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 开启悬浮窗，开启前会检查是否有权限
    public void startFloatingWindowService(View view){
        if(FloatingWindowService.isStarted){
            Log.d("Master_MainActivity", "FloatingWindowServiceStarted");
            return;
        }
        if(!Settings.canDrawOverlays(this)){
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainActivity.this.getPackageName())), OVERLAY_PERMISSION_REQ_CODE);
        }
        else{
            startService(new Intent(MainActivity.this, FloatingWindowService.class));
        }
    }
}
