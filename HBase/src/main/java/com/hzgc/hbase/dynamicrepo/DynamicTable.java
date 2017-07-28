package com.hzgc.hbase.dynamicrepo;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * 动态库表属性
 */
public class DynamicTable {

    public static final String TABLE_PERSON = "person";
    public static final byte[] PERSON_COLUMNFAMILY = Bytes.toBytes("i");
    public static final byte[] PERSON_COLUMN_IMGE = Bytes.toBytes("p");
    public static final byte[] PERSON_COLUMN_DESCRIBE = Bytes.toBytes("d");
    public static final byte[] PERSON_COLUMN_EXTRA = Bytes.toBytes("e");
    public static final byte[] PERSON_COLUMN_IPCID = Bytes.toBytes("f");
    public static final byte[] PERSON_COLUMN_TIMESTAMP = Bytes.toBytes("t");

    public static final String TABLE_PERFEA = "perFea";
    public static final byte[] PERFEA_COLUMNFAMILY = Bytes.toBytes("f");
    public static final byte[] PERFEA_COLUMN_FEA = Bytes.toBytes("fea");

    public static final String TABLE_CAR = "car";
    public static final byte[] CAR_COLUMNFAMILY = Bytes.toBytes("i");
    public static final byte[] CAR_COLUMN_IMGE = Bytes.toBytes("p");
    public static final byte[] CAR_COLUMN_DESCRIBE = Bytes.toBytes("d");
    public static final byte[] CAR_COLUMN_EXTRA = Bytes.toBytes("e");
    public static final byte[] CAR_COLUMN_IPCID = Bytes.toBytes("f");
    public static final byte[] CAR_COLUMN_PLATE = Bytes.toBytes("n");
    public static final byte[] CAR_COLUMN_TIMESTAMP = Bytes.toBytes("t");

    public static final String TABLE_CARFEA = "carFea";
    public static final byte[] CARFEA_COLUMNFAMILY = Bytes.toBytes("f");
    public static final byte[] CARFEA_COLUMN_FEA = Bytes.toBytes("fea");
    public static final byte[] CARFEA_COLUMN_PLATNUM = Bytes.toBytes("n");

}
