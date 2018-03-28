package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;

public class SrecordTable implements Serializable {
    public static final String TABLE_NAME ="srecord";  // 表示的是历史记录的表格名字
    public static final String RD_CLOF = "rd";        // 列族
    public static final String ROWKEY = "id";        // 历史记录唯一标志
    public static final String PLATFORM_ID = "platformid";     // 平台ID
    public static final String SEARCH_STATUS = "searchstatus";      // 搜索状态
    public static final String SEARCH_NUMS = "searchnums";      // 搜索出的总人员信息
    public static final String RESULTS = "results";      // 人员信息列表
    public static final String PHOTOID = "photoid";     // 照片ID
    public static final String PHOTO = "photo";          // 照片
    public static final String PSEARCH_MODEL = "searchmodel";     // 搜索模型。
}
