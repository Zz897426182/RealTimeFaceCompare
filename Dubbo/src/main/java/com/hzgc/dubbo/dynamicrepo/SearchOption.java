package com.hzgc.dubbo.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索选项
 */
public class SearchOption implements Serializable {
    private SearchType searchType;    //搜索类型，人PERSON（0）,车CAR（1）

    private List<PictureData> images; //待查询图片对象列表

    private boolean isOnePerson;    //是否将传入若干图片当做同一个人,不设置默认为false,即不是同一个人

    private String plateNumber;    //车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索

    private float threshold;    //阈值

    private List<String> deviceIds;    //搜索的设备范围

    private String platformId;    //平台 Id 优先使用 deviceIds 圈定范围

    private String startDate;    //开始日期,格式：xxxx-xx-xx xx:xx:xx

    private String endDate;    //截止日期,格式：xxxx-xx-xx xx:xx:xx

    private List<TimeInterval> intervals;    //搜索的时间区间，为空或者没有传入这个参数时候搜索整天

    private List<Attribute> attributes;    //参数筛选选项

    private List<SortParam> sortParams;    //排序参数

    private int offset;    //分页查询开始行

    private int count;    //查询条数

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public List<PictureData> getImages() {
        return images;
    }

    public void setImages(List<PictureData> images) {
        this.images = images;
    }

    public boolean isOnePerson() {
        return isOnePerson;
    }

    public void setOnePerson(boolean onePerson) {
        isOnePerson = onePerson;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<TimeInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<TimeInterval> intervals) {
        this.intervals = intervals;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<SortParam> getSortParams() {
        return sortParams;
    }

    public void setSortParams(List<SortParam> sortParams) {
        this.sortParams = sortParams;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "SearchOption{" +
                "searchType=" + searchType +
                ", plateNumber='" + plateNumber + '\'' +
                ", threshold=" + threshold +
                ", deviceIds=" + deviceIds +
                ", platformId='" + platformId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", intervals=" + intervals +
                ", attributes=" + attributes +
                ", sortParams='" + sortParams + '\'' +
                ", offset=" + offset +
                ", count=" + count +
                '}';
    }
}
