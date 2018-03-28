package com.hzgc.cluster.message;

import java.io.Serializable;

/**
 * 离线告警信息类（刘善彬）
 */
public class OffLineAlarmMessage implements Serializable {

    /**
     * 静态信息库id
     */
    private String staticID;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 识别更新时间
     */
    private String updateTime;

    /**
     * 告警推送时间
     */
    private String alarmTime;

    /**
     * 构造函数
     **/
    public OffLineAlarmMessage(String staticID, String alarmType, String updateTime, String alarmTime) {
        this.staticID = staticID;
        this.alarmType = alarmType;
        this.updateTime = updateTime;
        this.alarmTime = alarmTime;
    }

    public OffLineAlarmMessage() {
    }

    /**
     * Getter and Setter
     **/
    public String getStaticID() {
        return staticID;
    }

    public void setStaticID(String staticID) {
        this.staticID = staticID;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

}
