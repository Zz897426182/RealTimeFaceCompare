package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 搜索选项
 */
public class SearchOption implements Serializable {
    /**
     * 搜索类型，人PERSON（0）,车CAR（1）
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
    private List<String> deviceIds;
    /**
     * 平台 Id 优先使用 deviceIds 圈定范围
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
     * 搜索的时间区间，为空或者没有传入这个参数时候搜索整天
     */
    private List<TimeInterval> intervals;
    /**
     * 参数筛选选项
     */
    private List<SearchFilter> filters;
    /**
     * 排序参数
     */
    private String sortParams;

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<TimeInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<TimeInterval> intervals) {
        this.intervals = intervals;
    }

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public void init() {
        // 初始化搜索日期
    }

    public String getSortParams() {
        return sortParams;
    }

    public void setSortParams(String sortParams) {
        this.sortParams = sortParams;
    }

    @Override
    public String toString() {
        return "SearchOption{" +
                "searchType=" + searchType +
                ", image=" + Arrays.toString(image) +
                ", imageId='" + imageId + '\'' +
                ", plateNumber='" + plateNumber + '\'' +
                ", threshold=" + threshold +
                ", deviceIds=" + deviceIds +
                ", platformId='" + platformId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", intervals=" + intervals +
                ", filters=" + filters +
                ", sortParams=" + sortParams +
                '}';
    }
}


