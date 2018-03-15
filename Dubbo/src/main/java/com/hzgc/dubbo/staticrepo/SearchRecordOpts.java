package com.hzgc.dubbo.staticrepo;

import java.util.List;

/**
 * 历史查询的时候传过来的参数
 */
public class SearchRecordOpts {
    private String totalSearchId; // 查询ID
    private List<StaticSortParam> staticSortParams;  // 排序参数
    private List<SubQueryOpts> subQueryOptsList; // 子查询Id,以及子查询中按照对象类型分类的对象类型类表
    private int start;  // 返回条数中的起始位置
    private int size;   // 返回数据中的条数

    public String getTotalSearchId() {
        return totalSearchId;
    }

    public void setTotalSearchId(String totalSearchId) {
        this.totalSearchId = totalSearchId;
    }

    public List<StaticSortParam> getStaticSortParams() {
        return staticSortParams;
    }

    public void setStaticSortParams(List<StaticSortParam> staticSortParams) {
        this.staticSortParams = staticSortParams;
    }

    public List<SubQueryOpts> getSubQueryOptsList() {
        return subQueryOptsList;
    }

    public void setSubQueryOptsList(List<SubQueryOpts> subQueryOptsList) {
        this.subQueryOptsList = subQueryOptsList;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "SearchRecordOpts{" +
                "totalSearchId='" + totalSearchId + '\'' +
                ", staticSortParams=" + staticSortParams +
                ", subQueryOptsList=" + subQueryOptsList +
                ", start=" + start +
                ", size=" + size +
                '}';
    }
}
