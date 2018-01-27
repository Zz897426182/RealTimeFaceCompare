package com.hzgc.dubbo.address;

import java.util.List;

public interface MQSwitch {
    /**
     * 打开MQ接收数据
     *
     * @param userId    用户ID
     * @param time      时间戳
     * @param ipcIdList 设备ID列表
     */
    void openMQReception(String userId, long time, List<String> ipcIdList);

    /**
     * 关闭MQ接收数据
     *
     * @param userId 用户ID
     */
    void closeMQReception(String userId);
}
