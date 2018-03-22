package com.hzgc.dubbo.address;

import java.util.List;

/**
 * 人脸抓拍演示功能（过滤前端设备）
 */
public interface MQShow {
    /**
     * 打开MQ演示功能
     *
     * @param ipcIdList 设备列表
     */
    public void openMQShow(String userId, List<String> ipcIdList);

    /**
     * 关闭MQ演示功能
     *
     * @param userId
     */
    public void closeMQShow(String userId);

    /**
     * 查询需要演示的设备列表
     *
     * @return 设备列表
     */
    public List<String> getIpcId();
}
