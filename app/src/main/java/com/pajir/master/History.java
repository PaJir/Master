package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class History extends AppCompatActivity {
    private static final String TAG = "History";

    //private MasterDBHelper dbHelper = new MasterDBHelper(this, "master.db", null, 2);
    private MasterDBHelper dbHelper = new MasterDBHelper(this);

    private ArrayList<HashMap<String, String>> recordList = new ArrayList<>();


    private ArrayList<Integer> mHistoryId = new ArrayList<>();
    private ArrayList<String> mHistoryDuring = new ArrayList<>();
    private ArrayList<Integer> mHistorySum = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Log.d(TAG, "I will read database master and display");
        initData();
        initRecyclerView();
    }

    private void initData(){
        Log.d(TAG, "initData: from database");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Record", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            int i = 1;
            do{
                String time_from = cursor.getString(cursor.getColumnIndexOrThrow("time_from"));
                String time_end = cursor.getString(cursor.getColumnIndexOrThrow("time_end"));
                int time_length = cursor.getInt(cursor.getColumnIndexOrThrow("time_length"));
                mHistoryId.add(i);
                mHistoryDuring.add(time_from + " ~ " + time_end);
                mHistorySum.add(time_length);
                i += 1;
            } while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init RecyclerView");
        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        RecyclerView.Adapter adapter = new RecyclerViewAdapter(this, mHistoryId, mHistoryDuring, mHistorySum);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "initRecyclerView: init finished");
    }

    @Override
    protected void onDestroy(){
        dbHelper.close();
        super.onDestroy();
    }
}
