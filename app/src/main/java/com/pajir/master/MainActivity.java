package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final int OVERLAY_PERMISSION_REQ_CODE = 1;
    private FloatingWindowService.MyBinder mIBinder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIBinder = (FloatingWindowService.MyBinder) service;
            // 传参
            Spinner spinnerTime = (Spinner) findViewById(R.id.spinnerTime);
            int chosedTime = Integer.parseInt(spinnerTime.getSelectedItem().toString());
            mIBinder.getChosedTime(chosedTime);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIBinder = null;
        }
    };

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
        bindIntent.putExtra("chosedTime", chosedTime);
        startService(bindIntent);

        // 绑定的方法
        //Intent bindIntent = new Intent(this, FloatingWindowService.class);
        //bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        /*
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try{
            _data.writeInterfaceToken("FloatingWindowService");
            _data.writeInt(chosedTime);
            mIBinder.transact(0x001, _data, _reply, 0);
            _reply.readException();
            Log.d("Master_Main", _reply.readString());

        }catch(RemoteException e){
            e.printStackTrace();
        }finally {
            _reply.recycle();
            _data.recycle();
        }*/
    }

    // 关闭悬浮窗，应该用不到这个
    public void stopFloatingWindowService(View view){
        if(FloatingWindowService.isStarted){
            Log.d("Master_MainActivity", "I will stop service");
            stopService(new Intent(MainActivity.this, FloatingWindowService.class));
        }
    }
}
