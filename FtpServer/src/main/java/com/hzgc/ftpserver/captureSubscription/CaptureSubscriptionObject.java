package com.hzgc.ftpserver.captureSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 抓拍订阅对象
 */
public class CaptureSubscriptionObject {

    //zookeeper中保存的抓拍订阅设备信息
    Map<String, Map<String, List<String>>> captureSubscription;
    //抓拍订阅设备列表
    private volatile List<String> ipcIdList;

    public List<String> getIpcIdList() {
        return ipcIdList;
    }

    public void setIpcIdList(Map<String, Map<String, List<String>>> map) {
        List<String> ipcIdList = new ArrayList<>();
        if (!map.isEmpty()) {
            for (String userId : map.keySet()) {
                if (userId != null && !userId.equals("")) {
                    Map<String, List<String>> map1 = map.get(userId);
                    if (!map1.isEmpty()) {
                        for (String time : map1.keySet()) {
                            if (time != null && !time.equals("")) {
                                ipcIdList.addAll(map1.get(time));
                            }
                        }
                    }
                }
            }
        }
        this.ipcIdList = ipcIdList;
    }
}
