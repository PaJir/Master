package com.pajir.master;

import cn.bmob.v3.BmobObject;

public class BmobContact extends BmobObject {
    private static final String TAG = "BmobContact_Master";
    private String name;
    private String address;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
