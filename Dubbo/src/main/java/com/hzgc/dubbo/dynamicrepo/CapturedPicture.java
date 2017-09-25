package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * 动态图片定义
 */
public class CapturedPicture implements Serializable {

    /**
     * 图片 id (rowkey)用于获取图片
     */
    private String id;
    /**
     * 图片类型
     */
    private PictureType pictureType;
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
     * 图片数据
     */
    private byte[] smallImage;
    /**
     * 大图
     */
    private byte[] bigImage;
    /**
     * 时间戳
     */
    private long timeStamp;

    /**
     * 车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索
     */
    private String plateNumber;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PictureType getPictureType() {
        return pictureType;
    }

    public void setPictureType(PictureType pictureType) {
        this.pictureType = pictureType;
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

    public byte[] getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public byte[] getBigImage() {
        return bigImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
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
                "id='" + id + '\'' +
                ", pictureType=" + pictureType +
                ", ipcId='" + ipcId + '\'' +
                ", description='" + description + '\'' +
                ", similarity=" + similarity +
                ", extend=" + extend +
                ", smallImage=" + Arrays.toString(smallImage) +
                ", bigImage=" + Arrays.toString(bigImage) +
                ", timeStamp=" + timeStamp +
                ", plateNumber='" + plateNumber + '\'' +
                '}';
    }
}
