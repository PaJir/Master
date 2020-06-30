package com.pajir.master;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class RecordReceiver extends BroadcastReceiver {
    private final String TAG = "Master_Record";

    @Override
    public void onReceive(Context context, Intent intent){
        switch(intent.getAction()){
            case Intent.ACTION_BOOT_COMPLETED:
                Log.d("TAG", "reboot");
                new Restart();
                break;
        }
    }

    private class Restart extends AppCompatActivity{
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            MasterDBHelper dbHelper = new MasterDBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String time_from = null, time_end = null;
            int time_length = 0;
            Cursor cursor = db.query("Record", null, null, null, null, null, null);
            if(cursor.moveToFirst()){
                do{
                    time_from = cursor.getString(cursor.getColumnIndexOrThrow("time_from"));
                    time_end = cursor.getString(cursor.getColumnIndexOrThrow("time_end"));
                    time_length = cursor.getInt(cursor.getColumnIndexOrThrow("time_length"));
                } while(cursor.moveToNext());
            }
            cursor.close();
            if(time_from != null) {
                Intent bindIntent = new Intent(this, FloatingWindowService.class);
                bindIntent.putExtra("chosedTime", time_length);
                bindIntent.putExtra("startTime", time_from);

                startService(bindIntent);
            }
            dbHelper.close();
        }
    }
}
