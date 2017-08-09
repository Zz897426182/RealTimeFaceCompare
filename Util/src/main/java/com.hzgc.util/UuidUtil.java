package com.hzgc.util;

import java.util.UUID;

/**
 * Created by Administrator on 2017-8-3.
 */
public class UuidUtil {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public static String setUuid(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid;
    }
}
