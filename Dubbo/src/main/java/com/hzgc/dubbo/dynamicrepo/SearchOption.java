package com.hzgc.dubbo.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;

import java.io.Serializable;
import java.util.Arrays;
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
     * 开始日期,格式：xxxx-xx-xx xx:xx:xx
     */
    private String startDate;
    /**
     * 截止日期,格式：xxxx-xx-xx xx:xx:xx
     */
    private String endDate;
    /**
     * 搜索的时间区间，为空或者没有传入这个参数时候搜索整天
     */
    private List<TimeInterval> intervals;
    /**
     * 参数筛选选项
     */
    private List<Attribute> attributes;
    /**
     * 排序参数
     */
    private String sortParams;
    /**
     * 分页查询开始行
     */
    private int offset;
    /**
     * 查询条数
     */
    private int count;

    public SearchType getSearchType() {
        return searchType;
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

    public void setSortParams(String sortParams) {
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

    public String getSortParams() {
        return sortParams;
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
                ", attributes=" + attributes +
                ", sortParams='" + sortParams + '\'' +
                ", offset=" + offset +
                ", count=" + count +
                '}';
    }
}
