package com.hzgc.dubbo.device;

public class WarnRule {
    /**
     * 需要对比对象类型
     */
    String objectType;

    /**
     * 相似度阈值（0-100）
     */
    Integer threshold;

    /**
     * 天数阈值，只对离线告警起作用
     */
    Integer dayThreshold;

    /**
     * 告警类型
     * 0：代表识别告警，匹配到库中的数据产生告警，告警信息中需要有告警类型，对比库的 id
     * 1：代表新增告警，匹配不到库中的数据产生告警，告警信息中需要有告警类型，对比库的 id
     * 2：代表离线告警，假设 dayThreshold = 20 ，设备 20 天没有检测到这个人出现，意味着这个人可能离开这个小区了，
     * 需要产生离线告警，告警信息中需要有对比库 id ，以及离线数据的信息，时间段只对识别告警与新增告警起作用，离线告
     * 警需要全天匹配。
     */
    int code;
}
