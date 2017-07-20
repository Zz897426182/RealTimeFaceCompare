package com.hzgc.dubbo.dynamicrepo;


public class PersonPhoto {
    /**
     * 动态人脸照片ID
     */
    private String imageId;

    /**
     * 抓取照片的设备ID
     */
    private String ipc;
    /**
     * 人脸大图数据
     */
    private byte[] bigImage;

    /**
     * 人脸小图数据
     */
    private byte[] smallImage;

    /**
     * 图片描述信息
     */
    private String des;

    /**
     * 图片附加信息
     */
    private String ex;


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

    public byte[] getBigImage() {
        return bigImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
    }

    public byte[] getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
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



}
