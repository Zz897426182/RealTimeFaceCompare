package com.hzgc.service.device;

import java.util.Map;

public interface DeviceUtil {
    /**
     * 获取设备绑定的平台ID（内）（赵喆）
     *
     * @param ipcID 设备的ipcID
     * @return 平台ID，长度为0则未绑定平台ID
     */
    public String getplatfromID(String ipcID);

    /**
     * 获取设备所有布控规则（内）（赵喆）
     *
     * @param ipcID 设备ID
     * @return 设备绑定的布控规则的数据类型
     */
    public Map<Integer, Map<String, Integer>> isWarnTypeBinding(String ipcID);

    /**
     * 获取离线告警规则（内）（赵喆）
     *
     * @return 离线告警规则数据类型
     */
    public Map<String, Map<String, Integer>> getThreshold();
}
