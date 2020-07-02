package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class OpenRoomActivity extends AppCompatActivity {
    private static final String TAG = "OpenRoom_Master";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_room);
    }

    public void openRoom(View view){
        // TODO: 2020/7/2 enterRoom
        Log.d(TAG, "openRoom: todo");
    }
}
