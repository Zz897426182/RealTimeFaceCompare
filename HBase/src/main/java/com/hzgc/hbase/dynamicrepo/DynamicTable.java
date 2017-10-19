package com.hzgc.hbase.dynamicrepo;

import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;

/**
 * 动态库表属性
 */
public class DynamicTable implements Serializable {
    /**
     * person表
     */
    public static final String TABLE_PERSON = "person";
    /**
     * person表列簇
     */
    public static final byte[] PERSON_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 图片
     */
    public static final byte[] PERSON_COLUMN_IMGE = Bytes.toBytes("p");
    /**
     * 设备ID
     */
    public static final byte[] PERSON_COLUMN_IPCID = Bytes.toBytes("s");
    /**
     * 描述信息
     */
    public static final byte[] PERSON_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /**
     * 附加信息
     */
    public static final byte[] PERSON_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 时间戳
     */
    public static final byte[] PERSON_COLUMN_TIMESTAMP = Bytes.toBytes("t");
    /**
     * 特征值
     */
    public static final byte[] PERSON_COLUMN_FEA = Bytes.toBytes("f");

    /**
     * car表
     */
    public static final String TABLE_CAR = "car";
    /**
     * car表列簇
     */
    public static final byte[] CAR_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 图片
     */
    public static final byte[] CAR_COLUMN_IMGE = Bytes.toBytes("p");
    /**
     * 设备ID
     */
    public static final byte[] CAR_COLUMN_IPCID = Bytes.toBytes("s");
    /**
     * 描述信息
     */
    public static final byte[] CAR_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /***
     * 附加信息
     */
    public static final byte[] CAR_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 车牌号
     */
    public static final byte[] CAR_COLUMN_PLATENUM = Bytes.toBytes("n");
    /**
     * 时间戳
     */
    public static final byte[] CAR_COLUMN_TIMESTAMP = Bytes.toBytes("t");
    /**
     * 特征值
     */
    public static final byte[] CAR_COLUMN_FEA = Bytes.toBytes("f");

    /**
     * upFea表
     */
    public static final String TABLE_UPFEA = "upFea";
    /**
     * upFea表列簇-人
     */
    public static final byte[] UPFEA_PERSON_COLUMNFAMILY = Bytes.toBytes("p");
    /**
     * 图片-人
     */
    public static final byte[] UPFEA_PERSON_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * 特征值-人
     */
    public static final byte[] UPFEA_PERSON_COLUMN_FEA = Bytes.toBytes("f");
    /**
     * upFea表列簇-车
     */
    public static final byte[] UPFEA_CAR_COLUMNFAMILY = Bytes.toBytes("c");
    /**
     * 图片-车
     */
    public static final byte[] UPFEA_CAR_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * 特征值-车
     */
    public static final byte[] UPFEA_CAR_COLUMN_FEA = Bytes.toBytes("f");
    /**
     * 车牌号-车
     */
    public static final byte[] UPFEA_CAR_COLUMN_PLATENUM = Bytes.toBytes("n");
    /**
     * searchRes表
     */
    public static final String TABLE_SEARCHRES = "searchRes";
    /**
     * searchRes表列簇
     */
    public static final byte[] SEARCHRES_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 查询类型
     */
    public static final byte[] SEARCHRES_COLUMN_SEARCHTYPE = Bytes.toBytes("t");
    /**
     * IPCID表
     */
    public static final String TABLE_IPCID = "IPCID";
    /**
     * IPCID表列簇
     */
    public static final byte[] IPCID_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * ftpAddress
     */
    public static final byte[] IPCID_COLUMN_FTPADDRESS = Bytes.toBytes("a");
    /**
     * 只有人脸图
     */
    public static final String PERSON_TYPE = "PERSON";
    /**
     * 只有车辆图
     */
    public static final String CAR_TYPE = "CAR";
    /**
     * 同时包含人脸和车辆
     */
    public static final String MIX_TYPE = "MIX";

    /**
     * 查询信息
     */
    public static final byte[] SEARCHRES_COLUMN_SEARCHMESSAGE = Bytes.toBytes("m");
    // 以下是索引类型常量
    public static final String DYNAMIC_INDEX = "dynamic";
    public static final String PERSON_INDEX_TYPE = "person";

}
