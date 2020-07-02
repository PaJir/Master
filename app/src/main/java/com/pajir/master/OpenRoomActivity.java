package com.pajir.master;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class OpenRoomActivity extends AppCompatActivity {
    private static final String TAG = "OpenRoom_Master";

    private static int roomId = 0;

    private static long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_room);

        setToolbar();
    }

    // 以下与Toolbar有关
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.open_toolbar);
        setSupportActionBar(toolbar);
        // 0是label
        View titleView = toolbar.getChildAt(0);
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        titleView.setLayoutParams(layoutParams);
    }

    public void checkRoom(View view) {
        // 判断输入是否为空
        EditText editText = findViewById(R.id.editTextOpenId);
        String editTextString = editText.getText().toString();
        if (editTextString.length() == 0) {
            Toast.makeText(this, "请输入", Toast.LENGTH_SHORT).show();
            return;
        } else if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME) {
            Toast.makeText(this, "慢点慢点", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新点击时间
        // https://blog.csdn.net/hust_twj/article/details/78742453
        lastClickTime = System.currentTimeMillis();

        // 判断roomId是否已经存在
        roomId = Integer.parseInt(editTextString);
        BmobQuery<BmobContact> query = new BmobQuery<>();
        query.addWhereEqualTo("roomId", roomId);
        query.findObjects(new FindListener<BmobContact>() {
            @Override
            public void done(List<BmobContact> list, BmobException e) {
                if (e != null) {
                    Toast.makeText(OpenRoomActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                } else if (!list.isEmpty()) {
                    Toast.makeText(OpenRoomActivity.this, "该房间号已存在", Toast.LENGTH_SHORT).show();
                } else{
                    openRoom();
                }
            }
        });
    }

    private void openRoom(){
        // 设置参数
        Spinner spinner = findViewById(R.id.spinnerTimeOpen);
        // unit: second
        int length = Integer.parseInt(spinner.getSelectedItem().toString()) * getResources().getInteger(R.integer.sec_per_min);
        String startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());

        // 添加数据
        BmobContact bmobContact = new BmobContact();
        bmobContact.setRoomId(roomId);
        bmobContact.setStartTime(startTime);
        bmobContact.setLength(length);
        bmobContact.setOnlineSum(1);
        // 上传数据并开启服务
        bmobContact.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                Log.d(TAG, "done: "+objectId);
                if(e==null){
                    Intent intent = new Intent(OpenRoomActivity.this, OpenFloatingService.class);
                    intent.putExtra("chosedTime", length);
                    intent.putExtra("startTime", startTime);
                    intent.putExtra("objectId", objectId);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(OpenRoomActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
