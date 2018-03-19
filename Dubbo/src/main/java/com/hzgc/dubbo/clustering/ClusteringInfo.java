package com.hzgc.dubbo.clustering;


import java.io.Serializable;
import java.util.List;

/**
 * 建议迁入人口首页查询返回信息
 */
public class ClusteringInfo implements Serializable {
    private int totalClustering;
    private List<ClusteringAttribute> clusteringAttributeList;

    public int getTotalClustering() {
        return totalClustering;
    }

    public void setTotalClustering(int totalClustering) {
        this.totalClustering = totalClustering;
    }

    public List<ClusteringAttribute> getClusteringAttributeList() {
        return clusteringAttributeList;
    }

    public void setClusteringAttributeList(List<ClusteringAttribute> clusteringAttributeList) {
        this.clusteringAttributeList = clusteringAttributeList;
    }
}
