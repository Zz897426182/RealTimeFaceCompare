package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.List;

public class SearchResultOption implements Serializable {
    private String searchID;    //查询总ID
    private List<SingleResultOption> singleResultOptions;    //针对每个子ID查询的参数
    private List<SortParam> sortParam;    //总的排序参数
    private int start;    //查询起始位置
    private int limit;    //查多少条

    public String getSearchID() {
        return searchID;
    }

    public void setSearchID(String searchID) {
        this.searchID = searchID;
    }

    public List<SingleResultOption> getSingleResultOptions() {
        return singleResultOptions;
    }

    public void setSingleResultOptions(List<SingleResultOption> singleResultOptions) {
        this.singleResultOptions = singleResultOptions;
    }

    public List<SortParam> getSortParam() {
        return sortParam;
    }

    public void setSortParam(List<SortParam> sortParam) {
        this.sortParam = sortParam;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
