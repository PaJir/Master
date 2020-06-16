package com.pajir.master;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MasterDBHelper extends SQLiteOpenHelper {
    private Context context;
    private final static String DB_NAME = "master.db";
    private static final int DB_VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    private static final String SQL_CREATE_RECORD_TABLE = "CREATE TABLE IF NOT EXISTS" + MasterDBContract.RecordEntry.TABLE_NAME + " (" +
            MasterDBContract.RECORD_ID + TEXT_TYPE + COMMA_SEP + MasterDBContract.RecordEntry.TABLE_NAME + TEXT_TYPE + COMMA_SEP +
            MasterDBContract.RecordEntry.TIME_FROM +TEXT_TYPE + COMMA_SEP + MasterDBContract.RecordEntry.TIME_LENGTH + " )";

    public MasterDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_RECORD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
