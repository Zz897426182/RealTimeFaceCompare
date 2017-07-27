package com.hzgc.dubbo.dynamicrepo;


public class DynamicObject {
    /**
     * 动态人脸照片ID
     */
    private String imageId;

    /**
     * 抓取照片的设备ID
     */
    private String ipc;

    /**
     * 图片信息
     */
    private byte[] image;

    /**
     * 图片描述信息
     */
    private String des;

    /**
     * 图片附加信息
     */
    private String ex;

    /**
     * 时间戳
     */
    private long timeStamp;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getIpc() {
        return ipc;
    }

    public void setIpc(String ipc) {
        this.ipc = ipc;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getEx() {
        return ex;
    }

    public void setEx(String ex) {
        this.ex = ex;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
