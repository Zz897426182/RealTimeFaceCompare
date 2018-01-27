package com.hzgc.ftpserver.captureSubscription;

import com.hzgc.dubbo.address.MQSwitch;
import org.apache.log4j.Logger;

import java.util.*;

public class MQSwitchImpl extends MQSwitchInit implements MQSwitch {

    /**
     * 打开MQ接收数据
     *
     * @param userId    用户ID
     * @param time      时间戳
     * @param ipcIdList 设备ID列表
     */
    @Override
    public void openMQReception(String userId, long time, List<String> ipcIdList) {
        if (!userId.equals("") && !ipcIdList.isEmpty()) {
            String childPath = path + "/" + userId;
            StringBuilder data = new StringBuilder();
            data.append(userId).append(",").append(time).append(",");
            for (String ipcId : ipcIdList) {
                data.append(ipcId).append(",");
            }
            zookeeperClient.create(childPath, data.toString().getBytes());
        }
    }

    /**
     * 关闭MQ接收数据
     *
     * @param userId 用户ID
     */
    @Override
    public void closeMQReception(String userId) {
        if (!userId.equals("")) {
            zookeeperClient.delete(path + "/" + userId);
        }
    }
}
