package com.hzgc.dubbo.staticrepo;

import java.util.List;

/**
 * 子查询Id,以及子查询中按照对象类型分类的对象类型类表
 */
public class SubQueryOpts {
    private String queryId; // 子查询ID
    private List<String> pkeys; // 对象类型列表，用于按照对象类型分类。

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public List<String> getPkeys() {
        return pkeys;
    }

    public void setPkeys(List<String> pkeys) {
        this.pkeys = pkeys;
    }

    @Override
    public String toString() {
        return "SubQueryOpts{" +
                "queryId='" + queryId + '\'' +
                ", pkeys=" + pkeys +
                '}';
    }
}
