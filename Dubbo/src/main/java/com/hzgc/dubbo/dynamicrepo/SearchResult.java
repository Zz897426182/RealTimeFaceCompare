package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索结果
 */
public class SearchResult implements Serializable {
    /**
     * 本次搜索的 id
     */
    private String searchId;
    /**
     * 搜索结果数
     */
    private int total;
    /**
     * 匹配到的结果列表
     */

    private List<CapturedPicture> pictures;

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<CapturedPicture> getPictures() {
        return pictures;
    }

    public void setPictures(List<CapturedPicture> pictures) {
        this.pictures = pictures;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "searchId='" + searchId + '\'' +
                ", total=" + total +
                ", pictures=" + pictures +
                '}';
    }
}
