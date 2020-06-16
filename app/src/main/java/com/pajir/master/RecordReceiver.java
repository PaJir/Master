package com.pajir.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class RecordReceiver extends BroadcastReceiver {
    private final String TAG = "Master_Record";
    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals("RECORDFINISHED")) {

            Toast.makeText(context, "Congratulation! You are awesome!", Toast.LENGTH_SHORT).show();
        }
    }
}
