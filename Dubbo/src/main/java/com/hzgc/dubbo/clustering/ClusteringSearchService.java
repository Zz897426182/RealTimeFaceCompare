package com.hzgc.dubbo.clustering;


import java.util.List;

/**
 * 告警信息聚类查询接口（彭聪）
 */
public interface ClusteringSearchService {
    /**
     * 查询聚类信息（首页显示，只查询聚类的概要信息）
     *
     * @param time      聚类时间
     * @param start     返回数据下标开始符号
     * @param limit     行数
     * @param sortParam 排序参数（默认按出现次数排序）
     * @return 满足起始条件的聚类列表和聚类总数组成的对象ClusterInfo
     */
    ClusteringInfo clusteringSearch(String time, int start, int limit, String sortParam);

    /**
     * 查询单个聚类详细信息(告警记录)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    List<AlarmInfo> detailClusteringSearch(String clusterId, String time, int start, int limit, String sortParam);

    /**
     * 查询单个聚类详细信息(告警ID)
     *
     * @param clusterId 聚类ID
     * @param time      聚类时间
     * @param start     分页查询开始行
     * @param limit     查询条数
     * @param sortParam 排序参数（默认时间先后排序）
     * @return 返回该类下面所以告警信息
     */
    List<Integer> detailClusteringSearch_v1(String clusterId, String time, int start, int limit, String sortParam);

}
