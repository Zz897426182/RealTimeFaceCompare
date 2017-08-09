package com.hzgc.hbase.dynamicrepo;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * 动态库表属性
 */
public class DynamicTable {
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
     * 描述信息
     */
    public static final byte[] PERSON_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /**
     * 附加信息
     */
    public static final byte[] PERSON_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 设备ID
     */
    public static final byte[] PERSON_COLUMN_IPCID = Bytes.toBytes("f");
    /**
     * 时间戳
     */
    public static final byte[] PERSON_COLUMN_TIMESTAMP = Bytes.toBytes("t");
    /**
     * 特征值
     */
    public static final byte[] PERSON_COLUMN_FEA = Bytes.toBytes("fea");

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
     * 描述信息
     */
    public static final byte[] CAR_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /***
     * 附加信息
     */
    public static final byte[] CAR_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 设备ID
     */
    public static final byte[] CAR_COLUMN_IPCID = Bytes.toBytes("f");
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
    public static final byte[] CAR_COLUMN_FEA = Bytes.toBytes("fea");


    /**
     * upFea表
     */
    public static final String TABLE_UPFEA = "upFea";
    /**
     * upPerFea表列簇
     */
    public static final byte[] UPPERFEA_COLUMNFAMILY = Bytes.toBytes("p");
    /**
     * 小图
     */
    public static final byte[] UPPERFEA_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * 特征值
     */
    public static final byte[] UPPERFEA_COLUMN_FEA = Bytes.toBytes("fea");

    /**
     * upCarFea表列簇
     */
    public static final byte[] UPCARFEA_COLUMNFAMILY = Bytes.toBytes("c");
    /**
     * upCarFea小图
     */
    public static final byte[] UPCARFEA_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * upCarFea特征值
     */
    public static final byte[] UPCARFEA_COLUMN_FEA = Bytes.toBytes("fea");
    /**
     * 车牌号
     */
    public static final byte[] UPCARFEA_COLUMN_PLATENUM = Bytes.toBytes("n");

    /**
     * searchRes表
     */
    public static final String TABLE_SEARCHRES = "searchRes";
    /**
     * searchRes表列簇
     */
    public static final byte[] SEARCHRES_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 查询图片ID
     */
    public static final byte[] SEARCHRES_COLUMN_SEARCHIMAGEID = Bytes.toBytes("q");
    /**
     * 返回图片ID+相似度
     */
    public static final byte[] SEARCHRES_COLUMN_SEARCHMESSAGE = Bytes.toBytes("m");

}
