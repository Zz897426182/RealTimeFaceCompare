package com.hzgc.collect.expand.processer;

import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;

import java.io.Serializable;

/**
 * 人脸对象
 */
public class FaceObject implements Serializable {
    /**
     * 设备ID
     */
    private String ipcId;
    /**
     * 时间戳（格式：2017-01-01 00：00：00）
     */
    private String timeStamp;
    /**
     * 文件类型(区分人/车)
     */
    private SearchType type;
    /**
     * 日期（格式：2017-01-01）
     */
    private String date;
    /**
     * 时间段（格式：0000）(小时+分钟)
     */
    private String timeSlot;
    /**
     * 人脸属性对象
     */
    private FaceAttribute attribute;

    private String startTime;

    public FaceObject(String ipcId,
                      String timeStamp,
                      SearchType type,
                      String date,
                      String timeSlot,
                      FaceAttribute attribute,
                      String startTime) {
        this.ipcId = ipcId;
        this.timeStamp = timeStamp;
        this.type = type;
        this.date = date;
        this.timeSlot = timeSlot;
        this.attribute = attribute;
        this.startTime = startTime;
    }

    public String getIpcId() {
        return ipcId;
    }

    public void setIpcId(String ipcId) {
        this.ipcId = ipcId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public FaceAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(FaceAttribute attribute) {
        this.attribute = attribute;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


    @Override
    public String toString() {
        return "FaceObject{" +
                "ipcId='" + ipcId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", type=" + type +
                ", date='" + date + '\'' +
                ", timeSlot='" + timeSlot + '\'' +
                ", attribute=" + attribute +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}
