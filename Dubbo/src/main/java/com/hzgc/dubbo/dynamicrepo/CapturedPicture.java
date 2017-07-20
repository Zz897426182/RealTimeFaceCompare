package com.hzgc.dubbo.dynamicrepo;

import java.util.Arrays;
import java.util.Map;

/**
 * 动态图片定义
 */
public class CapturedPicture {

    /**
     * 图片 id (rowkey)用于获取图片
     */
    private String id;
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
    private int similarity;
    /**
     * 图片的附加信息，扩展预留
     */
    private Map<String, Object> extend;
    /**
     * 图片数据
     */
    private byte[] image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getSimilarity() {
        return similarity;
    }

    public void setSimilarity(int similarity) {
        this.similarity = similarity;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "CapturedPicture{" +
                "id='" + id + '\'' +
                ", ipcId='" + ipcId + '\'' +
                ", description='" + description + '\'' +
                ", similarity=" + similarity +
                ", extend=" + extend +
                ", image=" + Arrays.toString(image) +
                '}';
    }
}
