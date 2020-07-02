package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class EnterRoomActivity extends AppCompatActivity {
    private static final String TAG = "EnterRoom_Master";

    private static long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_room);

        setToolbar();
    }

    // 以下与Toolbar有关
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.enter_toolbar);
        setSupportActionBar(toolbar);
        // 0是label
        View titleView = toolbar.getChildAt(0);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        titleView.setLayoutParams(layoutParams);
    }

    public void checkRoom(View view){
        // 判断输入是否为空
        EditText editText = findViewById(R.id.editTextEnterId);
        String editTextString = editText.getText().toString();
        if (editTextString.length() == 0) {
            Toast.makeText(this, "请输入", Toast.LENGTH_SHORT).show();
            return;
        } else if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
            Toast.makeText(this, "慢点慢点", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新点击时间
        lastClickTime = System.currentTimeMillis();

        // 判断roomId是否已经存在
        int roomId = Integer.parseInt(editTextString);
        BmobQuery<BmobContact> query = new BmobQuery<>();
        query.addWhereEqualTo("roomId", roomId);
        query.findObjects(new FindListener<BmobContact>() {
            @Override
            public void done(List<BmobContact> list, BmobException e) {
                if (e != null) {
                    Toast.makeText(EnterRoomActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                } else if (list.isEmpty()) {
                    Toast.makeText(EnterRoomActivity.this, "该房间号不存在", Toast.LENGTH_SHORT).show();
                } else{
                    enterRoom(list.get(0));
                }
            }
        });
    }

    private boolean checkTime(String startTime){
        String curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date startDate = null;
        Date curDate = null;
        try{
            startDate = format.parse(startTime);
            curDate = format.parse(curTime);
        }catch (ParseException e){
            e.printStackTrace();
            return false;
        }
        long diff = curDate.getTime() - startDate.getTime();
        return diff < 65000;
    }

    private void enterRoom(BmobContact bmobContact){
        String startTime = bmobContact.getStartTime();
        Log.d(TAG, "enterRoom: " + startTime);

        if(!checkTime(startTime)){
            Toast.makeText(this, "已不能进入房间", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新数据并开启服务
        String objectId = bmobContact.getObjectId();
        bmobContact.setOnlineSum(bmobContact.getOnlineSum() + 1);
        bmobContact.update(objectId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                Log.d(TAG, "done: "+objectId);
                if(e==null){
                    Intent intent = new Intent(EnterRoomActivity.this, OpenFloatingService.class);
                    intent.putExtra("chosedTime", bmobContact.getLength());
                    intent.putExtra("startTime", startTime);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(EnterRoomActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
