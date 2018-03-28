package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ObjectSearchResult implements Serializable {
    private String searchId;  // 搜索id
    private int searchStatus;  // 查询成功与否状态，
    private String photoId;  // 基础图片的图片ID
    private long searchNums;  // 搜索出来的结果数量
    private List<Map<String, Object>> results;  // 搜索出来的人员或者车的详细信息

    public ObjectSearchResult() {
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public int getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(int searchStatus) {
        this.searchStatus = searchStatus;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public long getSearchNums() {
        return searchNums;
    }

    public void setSearchNums(long searchNums) {
        this.searchNums = searchNums;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }

    public void setResults(List<Map<String, Object>> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ObjectSearchResult{" +
                "searchId='" + searchId + '\'' +
                ", searchStatus='" + searchStatus + '\'' +
                ", photoId='" + photoId + '\'' +
                ", searchNums=" + searchNums +
                ", results=" + results +
                '}';
    }
}
