package com.hzgc.service.address;

import com.hzgc.collect.expand.subscribe.MQShowClient;
import com.hzgc.collect.expand.subscribe.ZookeeperParam;
import com.hzgc.collect.expand.util.ZookeeperClient;
import com.hzgc.dubbo.address.MQShow;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

public class MQShowImpl implements MQShow, Serializable {
    private static Logger LOG = Logger.getLogger(MQShowImpl.class);
    private ZookeeperClient zookeeperClient;

    public MQShowImpl() {
        zookeeperClient = new ZookeeperClient(ZookeeperParam.SESSION_TIMEOUT,
                ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_MQSHOW, ZookeeperParam.WATCHER);
    }

    /**
     * 打开MQ演示功能
     *
     * @param userId
     * @param ipcIdList 设备列表
     */
    @Override
    public void openMQShow(String userId, List<String> ipcIdList) {
        if (!userId.equals("") && !ipcIdList.isEmpty()) {
            String childPath = ZookeeperParam.PATH_MQSHOW + "/" + userId;
            long time = System.currentTimeMillis();
            StringBuilder data = new StringBuilder();
            data.append(userId).append(",").append(time).append(",");
            for (String ipcId : ipcIdList) {
                data.append(ipcId).append(",");
            }
            zookeeperClient.create(childPath, data.toString().getBytes());
        }
    }

    /**
     * 关闭MQ演示功能
     *
     * @param userId
     */
    @Override
    public void closeMQShow(String userId) {
        if (!userId.equals("")) {
            zookeeperClient.delete(ZookeeperParam.PATH_MQSHOW + "/" + userId);
        }
    }

    /**
     * 查询需要演示的设备列表
     *
     * @return 设备列表
     */
    @Override
    public List<String> getIpcId() {
        List<String> ipcIdList = new ArrayList<>();
        MQShowClient mqShowClient = new MQShowClient(ZookeeperParam.SESSION_TIMEOUT, ZookeeperParam.zookeeperAddress, ZookeeperParam.PATH_MQSHOW, false);
        mqShowClient.createConnection(ZookeeperParam.zookeeperAddress, ZookeeperParam.SESSION_TIMEOUT);
        List<String> children = mqShowClient.getChildren();
        if (!children.isEmpty()) {
            for (String child : children) {
                String childPath = ZookeeperParam.PATH_MQSHOW + "/" + child;
                byte[] data = mqShowClient.getDate(childPath);
                if (data != null) {
                    String ipcIds = new String(data);
                    if (!ipcIds.equals("") && ipcIds.contains(",") && ipcIds.split(",").length >= 3) {
                        ipcIds = ipcIds.substring(0, ipcIds.length() - 1);
                        List<String> list = Arrays.asList(ipcIds.split(","));
                        ipcIdList = new ArrayList<>();
                        for (int i = 2; i < list.size(); i++) {
                            ipcIdList.add(list.get(i));
                        }
                    }
                }
            }
        }
        return ipcIdList;
    }
}
