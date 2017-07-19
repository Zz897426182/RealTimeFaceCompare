package com.hzgc.dubbo.dynamic;

/**
 * 搜索过滤条件
 */
public class SearchFilter {
    /**
     * 过滤参数
     */
    private String param;
    /**
     * true 筛选不符合条件的选项，false 筛选符合条件的选项
     */
    private boolean inverse = false;
    /**
     * 与其他条件的拼接运算，默认是 OR 运算
     */
    private SearchOperation option = SearchOperation.OR;

    /**
     * 搜索条件的拼接运算
     */
    public enum SearchOperation{
        AND,
        OR
    }

}
