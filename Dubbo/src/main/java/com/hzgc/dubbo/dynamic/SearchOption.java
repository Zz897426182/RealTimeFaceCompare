package com.hzgc.dubbo.dynamic;

import java.util.Date;
import java.util.List;

/**
 * 搜索选项
 */
public class SearchOption {
    /**
     * 搜索类型
     */
    private SearchType searchType;
    /**
     * 图片的二进制数据
     */
    private byte[] image;
    /**
     * 图片 id ,优先使用图片流数组
     */
    private String imageId;
    /**
     * 车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索
     */
    private String plateNumber;
    /**
     * 阈值
     */
    private float threshold;
    /**
     * 搜索的设备范围
     */
    private List<String> boxChannelIds;
    /**
     * 平台 Id 优先使用 boxChannelIds 圈定范围
     */
    private String platformId;
    /**
     * 开始日期
     */
    private Date startDate;
    /**
     * 截止日期
     */
    private Date endDate;
    /**
     * 搜索的时间区间，为空搜索整天
     */
    private List<TimeInterval> intervals;
    /**
     * 参数筛选选项
     */
    private List<SearchFilter> filters;

    public void init() {
        // 初始化搜索日期

    }

}


