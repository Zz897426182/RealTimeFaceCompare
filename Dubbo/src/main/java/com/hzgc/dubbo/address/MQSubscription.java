package com.hzgc.dubbo.address;

import java.util.List;

/**
 * 人脸抓拍订阅功能（过滤前端设备）
 */
public interface MQSubscription {
    /**
     * 打开MQ接收数据
     *
     * @param userId    用户ID
     * @param ipcIdList 设备ID列表
     */
    void openMQReception(String userId, List<String> ipcIdList);

    /**
     * 关闭MQ接收数据
     *
     * @param userId 用户ID
     */
    void closeMQReception(String userId);
}
