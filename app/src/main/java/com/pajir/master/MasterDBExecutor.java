package com.pajir.master;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MasterDBExecutor {
    private Context context;
    private MasterDBHelper dbHelper;
    SQLiteDatabase database = null;

    public MasterDBExecutor(Context context){
        this.context = context;
        this.dbHelper = new MasterDBHelper(context);
    }

    public void open(){
        if((database == null || !database.isOpen() && dbHelper != null)){
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close(){
        if(database != null){
            database.close();
        }
    }

}
