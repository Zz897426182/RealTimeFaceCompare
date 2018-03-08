package com.hzgc.dubbo.clustering;

import java.io.Serializable;

/**
 * 每个聚类的信息，每个聚类作为一个对象，包含显示图片url，最早、晚出现时间及ipcId和该聚类中图片数量count(彭聪)
 */
public class ClusteringAttribute implements Serializable {
    /**
     * 聚类ID
     */
    private String clusteringId;
    /**
     * 首页显示图片URL
     */
    private String ftpUrl;
    /**
     * 首次出现时间
     */
    private String firstAppearTime;
    /**
     * 首次出现图片IPcId
     */
    private String firstIpcId;
    /**
     * 最后出现时间
     */
    private String lastAppearTime;
    /**
     * 最后出现图片IPcId
     */
    private String lastIpcId;
    /**
     * 次数
     */
    private int count;

    public String getClusteringId() {
        return clusteringId;
    }

    public void setClusteringId(String clusteringId) {
        this.clusteringId = clusteringId;
    }

    public String getFtpUrl() {
        return ftpUrl;
    }

    public void setFtpUrl(String ftpUrl) {
        this.ftpUrl = ftpUrl;
    }

    public String getFirstAppearTime() {
        return firstAppearTime;
    }

    public void setFirstAppearTime(String firstAppearTime) {
        this.firstAppearTime = firstAppearTime;
    }

    public String getFirstIpcId() {
        return firstIpcId;
    }

    public void setFirstIpcId(String firstIpcId) {
        this.firstIpcId = firstIpcId;
    }

    public String getLastAppearTime() {
        return lastAppearTime;
    }

    public void setLastAppearTime(String lastAppearTime) {
        this.lastAppearTime = lastAppearTime;
    }

    public String getLastIpcId() {
        return lastIpcId;
    }

    public void setLastIpcId(String lastIpcId) {
        this.lastIpcId = lastIpcId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


}
