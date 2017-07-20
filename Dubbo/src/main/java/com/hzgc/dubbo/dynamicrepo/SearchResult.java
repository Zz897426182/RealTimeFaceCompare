package com.hzgc.dubbo.dynamicrepo;

import java.util.List;

/**
 * 搜索结果
 */
public class SearchResult {
    /**
     * 本次搜索的 id
     */
    private String searchId;
    /**
     * 是否搜索完成
     */
    private boolean finished;
    /**
     * 搜索图片的 id
     */
    private String imageId;
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

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
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
                ", finished=" + finished +
                ", imageId='" + imageId + '\'' +
                ", total=" + total +
                ", pictures=" + pictures +
                '}';
    }
}
