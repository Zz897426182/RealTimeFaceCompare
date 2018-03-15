package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Map;

/**
 * 动态图片定义
 */
public class CapturedPicture implements Serializable {

    /**
     * 小图url
     */
    private String surl;
    /**
     * 大图url
     */
    private String burl;
    /**
     * 捕获照片的设备 id
     */
    private String ipcId;
    /**
     * 图片的描述信息
     */
    private String description;
    /**
     * 图片的相似度
     */
    private Float similarity;
    /**
     * 图片的附加信息，扩展预留
     */
    private Map<String, Object> extend;
    /**
     * 时间戳
     */
    private String timeStamp;
    /**
     * 车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索
     */
    private String plateNumber;

    public String getSurl() {
        return surl;
    }

    public void setSurl(String surl) {
        this.surl = surl;
    }

    public String getBurl() {
        return burl;
    }

    public void setBurl(String burl) {
        this.burl = burl;
    }

    public String getIpcId() {
        return ipcId;
    }

    public void setIpcId(String ipcId) {
        this.ipcId = ipcId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    @Override
    public String toString() {
        return "CapturedPicture{" +
                "surl='" + surl + '\'' +
                ", burl='" + burl + '\'' +
                ", ipcId='" + ipcId + '\'' +
                ", description='" + description + '\'' +
                ", similarity=" + similarity +
                ", extend=" + extend +
                ", timeStamp='" + timeStamp + '\'' +
                ", plateNumber='" + plateNumber + '\'' +
                '}';
    }
}
