package com.pajir.master;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MasterDBHelper extends SQLiteOpenHelper {
    private final String TAG = "Master_SQLHelper";

    private static final String DATABASE_NAME = "master.db";
    private static final int DATABASE_VERSION = 4;
    private Context context;

    private static final String CREATE_RECORD_TABLE = "create table if not exists Record ("
            + "id integer primary key autoincrement, "
            + "time_from text, "
            + "time_end text, "
            + "time_length int)";

    // 没有bug的话，这张表只有一个数据，便于开机时读取
    private static final String CREATE_CUR_RECORD_TABLE = "create table if not exists CurRecord ("
            + "id integer primary key autoincrement, "
            + "time_from text, "
            + "time_end text, "
            + "time_length int)";

    public MasterDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public MasterDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_RECORD_TABLE);
        db.execSQL(CREATE_CUR_RECORD_TABLE);
        Log.d(TAG, "create db successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //db.execSQL("drop table if exists Record");
        Log.d(TAG, "drop db successfully");
        onCreate(db);
    }
}
