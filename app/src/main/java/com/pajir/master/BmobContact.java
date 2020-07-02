package com.pajir.master;

import cn.bmob.v3.BmobObject;

public class BmobContact extends BmobObject {
    private static final String TAG = "BmobContact_Master";
    private int roomId;
    private String startTime;
    private int length;
    private int onlineSum;

    public int getId() {
        return roomId;
    }
    public void setRoomId(int roomId){
        this.roomId = roomId;
    }
    public String getStartTime(){
        return startTime;
    }
    public void setStartTime(String startTime){
        this.startTime = startTime;
    }
    public int getLength(){
        return length;
    }
    public void setLength(int length){
        this.length = length;
    }
    public int getOnlineSum(){
        return onlineSum;
    }
    public void setOnlineSum(int onlineSum){
        this.onlineSum = onlineSum;
    }
}
