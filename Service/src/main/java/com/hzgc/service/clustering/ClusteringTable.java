package com.hzgc.service.clustering;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * 聚类有关的HBase表
 */
public class ClusteringTable {
    //clusteringInfo表
    public static final String TABLE_ClUSTERINGINFO = "clusteringInfo";
    //clusteringInfo表列簇
    public static final byte[] ClUSTERINGINFO_COLUMNFAMILY = Bytes.toBytes("c");
    //聚类信息
    public static final byte[] ClUSTERINGINFO_COLUMN_YES = Bytes.toBytes("y");
    //
    public static final byte[] ClUSTERINGINFO_COLUMN_NO = Bytes.toBytes("n");
    //detailInfo表
    public static final String TABLE_DETAILINFO = "detailInfo";
    //detailInfo表列簇
    public static final byte[] DETAILINFO_COLUMNFAMILY = Bytes.toBytes("c");
    //告警详细信息
    public static final byte[] DETAILINFO_COLUMN_DATA = Bytes.toBytes("i");
}
