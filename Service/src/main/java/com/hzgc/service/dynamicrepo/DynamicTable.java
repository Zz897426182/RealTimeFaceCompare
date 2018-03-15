package com.hzgc.service.dynamicrepo;

import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;

/**
 * 动态库表属性
 */
public class DynamicTable implements Serializable {

    //upFea表
    static final String TABLE_UPFEA = "upFea";
    //upFea表列簇-人
    static final byte[] UPFEA_PERSON_COLUMNFAMILY = Bytes.toBytes("p");
    //图片-人
    static final byte[] UPFEA_PERSON_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    //特征值-人
    static final byte[] UPFEA_PERSON_COLUMN_FEA = Bytes.toBytes("f");
    //upFea表列簇-车
    static final byte[] UPFEA_CAR_COLUMNFAMILY = Bytes.toBytes("c");
    //图片-车
    static final byte[] UPFEA_CAR_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    //特征值-车
    static final byte[] UPFEA_CAR_COLUMN_FEA = Bytes.toBytes("f");
    //车牌号-车 保留字段
    public static final byte[] UPFEA_CAR_COLUMN_PLATENUM = Bytes.toBytes("n");
    //searchRes表
    static final String TABLE_SEARCHRES = "searchRes";
    //searchRes表列簇
    static final byte[] SEARCHRES_COLUMNFAMILY = Bytes.toBytes("i");
    //查询类型
    static final byte[] SEARCHRES_COLUMN_SEARCHTYPE = Bytes.toBytes("t");
    //只有人脸图
    static final String PERSON_TYPE = "PERSON";
    //只有车辆图
    static final String CAR_TYPE = "CAR";
    //同时包含人脸和车辆 保留字段
    public static final String MIX_TYPE = "MIX";
    //查询信息
    static final byte[] SEARCHRES_COLUMN_SEARCHMESSAGE = Bytes.toBytes("m");
    //es索引
    public static final String DYNAMIC_INDEX = "dynamic";
    //es类型
    public static final String PERSON_INDEX_TYPE = "person";
    //图片的ftp地址 xxx/xxx/xxx/
    static final String FTPURL = "ftpurl";
    //设备id
    public static final String IPCID = "ipcid";
    //时间区间 数据格式 小时+分钟 例如:11:30用1130表示
    public static final String TIMESLOT = "timeslot";
    //时间戳 数据格式 xxxx-xx-xx xx:xx:xx(年-月-日 时:分:秒)
    public static final String TIMESTAMP = "exacttime";
    //图片类型
    public static final String SEARCHTYPE = "searchtype";
    //日期 分区字段 数据格式 xxxx-xx-xx(年-月-日)
    public static final String DATE = "date";
    //人脸属性-是否戴眼镜
    public static final String ELEGLASSES = "eleglasses";
    //人脸属性-性别 男或女
    public static final String GENDER = "gender";
    //人脸属性-头发颜色
    public static final String HAIRCOLOR = "haircolor";
    //人脸属性-头发类型
    public static final String HAIRSTYLE = "hairstyle";
    //人脸属性-是否带帽子
    public static final String HAT = "hat";
    //人脸属性-胡子类型
    public static final String HUZI = "huzi";
    //人脸属性-是否带领带
    public static final String TIE = "tie";
    //小文件合并后数据表
    static final String PERSON_TABLE = "person_table";
    //小文件合并前数据表
    static final String MID_TABLE = "mid_table";
    //特征值
    static final String FEATURE = "feature";
    //特征值比对结果相似度
    static final String SIMILARITY = "similarity";
    //hive中特征值比对udf函数
    static final String FUNCTION_NAME = "compare";

    static final String GROUP_FIELD = "id";
}
