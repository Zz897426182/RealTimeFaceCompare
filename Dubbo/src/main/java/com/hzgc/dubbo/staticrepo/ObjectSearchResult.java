package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 静态库查询的时候返回的结果
 */
public class ObjectSearchResult implements Serializable {
    private String searchTotalId; //总的searchId
    private int searchStatus;  // 查询成功与否状态，
    private List<PersonSingleResult> finalResults; //最终需要返回的结果，String是分别的Id

    public ObjectSearchResult() {
    }

    public String getSearchTotalId() {
        return searchTotalId;
    }

    public void setSearchTotalId(String searchTotalId) {
        this.searchTotalId = searchTotalId;
    }

    public int getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(int searchStatus) {
        this.searchStatus = searchStatus;
    }

    public List<PersonSingleResult> getFinalResults() {
        return finalResults;
    }

    public void setFinalResults(List<PersonSingleResult> finalResults) {
        this.finalResults = finalResults;
    }

    @Override
    public String toString() {
        return "ObjectSearchResult{" +
                "searchTotalId='" + searchTotalId + '\'' +
                ", searchStatus=" + searchStatus +
                ", finalResults=" + finalResults +
                '}';
    }
}
