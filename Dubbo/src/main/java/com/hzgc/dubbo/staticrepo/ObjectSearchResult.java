package com.hzgc.dubbo.staticrepo;

import java.util.List;

/**
 * Created by lenovo on 2017/7/11.
 */
public class ObjectSearchResult {
    private String searchId;  // 搜索id
    private String searchStatus;  // 查询成功与否状态，
    private String photoId;  // 基础图片的图片ID
    private long searchNums;  // 搜索出来的结果数量
    private List<ObjectInfo> results;  // 搜索出来的人员或者车的详细信息

    public ObjectSearchResult() {
    }

    public ObjectSearchResult(String searchId, String searchStatus,
                              String photoId, long searchNums,
                              List<ObjectInfo> results) {
        this.searchId = searchId;
        this.searchStatus = searchStatus;
        this.photoId = photoId;
        this.searchNums = searchNums;
        this.results = results;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(String searchStatus) {
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

    public List<ObjectInfo> getResults() {
        return results;
    }

    public void setResults(List<ObjectInfo> results) {
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
