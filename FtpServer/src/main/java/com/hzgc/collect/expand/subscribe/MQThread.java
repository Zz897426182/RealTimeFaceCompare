package com.hzgc.collect.expand.subscribe;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * 人脸抓拍订阅功能及人脸抓拍演示功能定时刷新及去除过期数据
 */
public class MQThread extends ReceiveObject implements Serializable {
    private static Logger LOG = Logger.getLogger(MQThread.class);
    private MQSubscriptionClient mqSubscriptionClient;
    private MQShowClient mqShowClient;
    private ReceiveObject object = ReceiveObject.getInstance();

    public MQThread() {
        mqSubscriptionClient = new MQSubscriptionClient(ZookeeperParam.SESSION_TIMEOUT, ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_SUBSCRIBE, ZookeeperParam.WATCHER);
        mqSubscriptionClient.createConnection(ZookeeperParam.zookeeperAddress, ZookeeperParam.SESSION_TIMEOUT);
        mqShowClient = new MQShowClient(ZookeeperParam.SESSION_TIMEOUT, ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_MQSHOW, ZookeeperParam.WATCHER);
        mqShowClient.createConnection(ZookeeperParam.zookeeperAddress, ZookeeperParam.SESSION_TIMEOUT);
    }

    private boolean isInDate(String time) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, -6);
        long endTime = now.getTimeInMillis();
        long startTime = Long.parseLong(time);
        return startTime <= endTime;
    }

    /**
     * 定位功能
     *
     * @return true表示当前属于订阅功能，false表示当前属于演示功能
     */
    private boolean isShow() {
        List<String> children = mqShowClient.getChildren();
        return children.isEmpty();
    }

    public void start() {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    if (isShow()) {
                        map_ZKData = mqSubscriptionClient.getData();
                        object.setIpcIdList_subscription(map_ZKData);
                        for (String userId : map_ZKData.keySet()) {
                            Map<String, List<String>> map = map_ZKData.get(userId);
                            for (String time : map.keySet()) {
                                if (isInDate(time)) {
                                    mqSubscriptionClient.delete(ZookeeperParam.PATH_SUBSCRIBE + "/" + userId);
                                }
                            }
                        }
                        LOG.info("OpenShow = false, ipcIdList:" + object.getIpcIdList_subscription());
                    } else {
                        map_ZKData = mqShowClient.getData();
                        object.setIpcIdList_show(map_ZKData);
                        for (String userId : map_ZKData.keySet()) {
                            Map<String, List<String>> map = map_ZKData.get(userId);
                            for (String time : map.keySet()) {
                                if (isInDate(time)) {
                                    mqShowClient.delete(ZookeeperParam.PATH_MQSHOW + "/" + userId);
                                }
                            }
                        }
                        LOG.info("OpenShow = true, ipcIdList:" + object.getIpcIdList_show());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error("MQstart thread error!");
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        LOG.info("The face snapshot subscription function starts successfully!");
    }
}
