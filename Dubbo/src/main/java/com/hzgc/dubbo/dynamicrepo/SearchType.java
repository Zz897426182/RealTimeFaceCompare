package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;

/**
 * 搜索类型，搜人或者搜车
 */
public enum SearchType implements Serializable {
    /**
     * 搜索类型为人
     */
    PERSON,
    /**
     * 搜索类型为车
     */
    CAR
}

