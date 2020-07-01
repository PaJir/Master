package com.pajir.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
    private static final String TAG = "RecordReceiver_Master";

    @Override
    public void onReceive(Context context, Intent intent){
        switch(intent.getAction()){
            case Intent.ACTION_LOCKED_BOOT_COMPLETED:
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_REBOOT:
                Log.d("TAG", "reboot");
                Intent intent1 = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
        }
    }
}
