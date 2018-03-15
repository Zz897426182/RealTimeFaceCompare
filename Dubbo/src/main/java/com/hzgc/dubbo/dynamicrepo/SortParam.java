package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;

public enum  SortParam implements Serializable {
    IPC,    // 按设备排序
    TIMEASC,     //时间升序
    TIMEDESC,    //时间降序
    SIMDASC,     //相似度升序
    SIMDESC,    //相似度降序
}

