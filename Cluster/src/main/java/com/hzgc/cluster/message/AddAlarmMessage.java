package com.hzgc.cluster.message;

import java.io.Serializable;

/**
 * 新增告警推送信息类（刘善彬）
 */
public class AddAlarmMessage implements Serializable {

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 抓取人脸的设备id
     */
    private String dynamicDeviceID;

    /**
     * 告警推送时间
     */
    private String alarmTime;

    /**
     * 动态抓取照片的存储主机名(新增字段)
     */
    private String hostName;

    /**
     * 动态抓取人脸大图URL(新增字段)
     */
    private String bigPictureURL;

    /**
     * 动态抓取人脸小图URL(新增字段)
     */
    private String smallPictureURL;

    /**
     * 类构造函数
     *
     * @param alarmType
     * @param dynamicDeviceID
     * @param alarmTime
     */
    public AddAlarmMessage(String alarmType, String dynamicDeviceID, String alarmTime, String hostName, String bigPictureURL, String smallPictureURL) {
        this.alarmType = alarmType;
        this.dynamicDeviceID = dynamicDeviceID;
        this.alarmTime = alarmTime;
        this.hostName = hostName;
        this.bigPictureURL = bigPictureURL;
        this.smallPictureURL = smallPictureURL;
    }
    public AddAlarmMessage() {
    }

    /**
     * Getter and Setter
     **/
    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getDynamicDeviceID() {
        return dynamicDeviceID;
    }

    public void setDynamicDeviceID(String dynamicDeviceID) {
        this.dynamicDeviceID = dynamicDeviceID;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBigPictureURL() {
        return bigPictureURL;
    }

    public void setBigPictureURL(String bigPictureURL) {
        this.bigPictureURL = bigPictureURL;
    }

    public String getSmallPictureURL() {
        return smallPictureURL;
    }

    public void setSmallPictureURL(String smallPictureURL) {
        this.smallPictureURL = smallPictureURL;
    }
}
