package com.hzgc.dubbo.staticrepo;

import java.io.Serializable;

public class ObjectInfoTable implements Serializable {
    public static final String TABLE_NAME = "objectinfo"; // 表名
    public static final String ROWKEY = "id";              // 对象信息的唯一标志
    public static final String PERSON_COLF = "person";  // person列族
    public static final String NAME_PIN = "namepin";    // 姓名的拼音
    public static final String CREATOR_PIN = "creatorpin";  // 创建者的拼音
    public static final String TOTAL_NUMS_ROW_NAME = "99999999999999999999999999";  // 保存总的记录数的行的名字
    public static final String TOTAL_NUMS = "tnums";  // 总的记录数

    // 以下是和平台组公用的属性
    public static final String PLATFORMID = "platformid";  // 平台ID
    public static final String TAG = "tag";            // 人车标志
    public static final String PKEY = "pkey";          // 人员类型
    public static final String NAME = "name";         // 姓名
    public static final String IDCARD = "idcard";    // 身份证号
    public static final String SEX = "sex";           //  性别
    public static final String PHOTO = "photo";         // 照片
    public static final String FEATURE = "feature";  // 特征值
    public static final String REASON = "reason";     // 理由
    public static final String CREATOR = "creator";    // 布控人
    public static final String CPHONE = "cphone";      // 布控人手机号
    public static final String CREATETIME = "createtime";     // 创建时间
    public static final String UPDATETIME = "updatetime";     // 更新时间
    public static final String RELATED = "related";        // 相关度
    public static final String IMPORTANT = "important";  // 是否重点关注人员，0，是，1，不是
    public static final String STATUS = "status";  // 人员状态，0，常住人口，1，建议迁出
}
