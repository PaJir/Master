package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class History extends AppCompatActivity {
    private final String TAG = "Master_History";

    //private MasterDBHelper dbHelper = new MasterDBHelper(this, "master.db", null, 2);
    private MasterDBHelper dbHelper = new MasterDBHelper(this);

    private ArrayList<HashMap<String, String>> recordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Log.d(TAG, "I will read database master");
        displayListView();
    }

    private void displayListView(){
        ListView listView = (ListView) findViewById(R.id.listViewRecord);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Record", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                String time_from = cursor.getString(cursor.getColumnIndexOrThrow("time_from"));
                String time_end = cursor.getString(cursor.getColumnIndexOrThrow("time_end"));
                int time_length = cursor.getInt(cursor.getColumnIndexOrThrow("time_length"));
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("re", time_from + " - " + time_end + ", the duration is " + time_length + " min");
                recordList.add(hashMap);
            } while(cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, "I will read database master7");
        String[] from = {"re"};
        int[] value = {R.id.textViewRecord};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, recordList, R.layout.record_item, from, value);
        listView.setAdapter(simpleAdapter);
    }

    @Override
    protected void onDestroy(){
        dbHelper.close();
        super.onDestroy();
    }
}
