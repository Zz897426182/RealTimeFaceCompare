package com.hzgc.util.common.sort;

import java.io.Serializable;
import java.util.Arrays;

public class SortParam implements Serializable {
    /**
     * 排序参数名称
     */
    private String[] sortNameArr;
    /**
     * 排序方式（升序or降序）
     */
    private boolean[] isAscArr;

    public String[] getSortNameArr() {
        return sortNameArr;
    }

    public void setSortNameArr(String[] sortNameArr) {
        this.sortNameArr = sortNameArr;
    }

    public boolean[] getIsAscArr() {
        return isAscArr;
    }

    public void setIsAscArr(boolean[] isAscArr) {
        this.isAscArr = isAscArr;
    }

    @Override
    public String toString() {
        return "SortParam{" +
                "sortNameArr=" + Arrays.toString(sortNameArr) +
                ", isAscArr=" + Arrays.toString(isAscArr) +
                '}';
    }
}
