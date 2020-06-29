package com.pajir.master;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class RecordReceiver extends BroadcastReceiver {
    private final String TAG = "Master_Record";

    private MasterDBHelper dbHelper;
    private Restart restart;

    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals("RECORDFINISHED")) {
            dbHelper = new MasterDBHelper(context);
            Toast.makeText(context, "Congratulation! You are awesome!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "recerive record finished");
            //MasterDBHelper dbHelper = new MasterDBHelper(context, "master.db", null, 3);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("time_from",intent.getStringExtra("time_from"));
            values.put("time_end",intent.getStringExtra("time_end"));
            values.put("time_length",intent.getIntExtra("time_length", 0));
            db.insert("Record", null, values);

            db.delete("CurRecord", "id >= ?", new String[] {"0"});
            dbHelper.close();
        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Log.d("TAG", "reboot");
            restart = new Restart();
        }
        else if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
            Toast.makeText(context, "airplane mode changed", Toast.LENGTH_SHORT).show();
        }
    }

    private class Restart extends AppCompatActivity{
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            dbHelper = new MasterDBHelper(this);
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
