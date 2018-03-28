package com.hzgc.service.device;

import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;

public class DeviceTable implements Serializable {
    final static String TABLE_DEVICE = "device";
    final static byte[] CF_DEVICE = Bytes.toBytes("device");
    final static byte[] PLAT_ID = Bytes.toBytes("p");
    final static byte[] NOTES = Bytes.toBytes("n");
    final static byte[] WARN = Bytes.toBytes("w");
    final static byte[] OFFLINERK = Bytes.toBytes("offlineWarnRowKey");
    final static byte[] OFFLINECOL = Bytes.toBytes("objTypes");
    public final static Integer IDENTIFY = 100;
    public final static Integer ADDED = 101;
    public final static Integer OFFLINE = 102;

}