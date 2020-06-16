package com.pajir.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class RecordReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals("RECORDFINISHED")) {

            Toast.makeText(context, "Received", Toast.LENGTH_SHORT).show();
        }
    }
}
