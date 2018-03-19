package com.hzgc.cluster.clustering

import java.util
import java.util.List

import com.hzgc.dubbo.clustering.{AlarmInfo, ClusteringAttribute}
import com.hzgc.service.clustering.ClusteringTable
import com.hzgc.service.util.HBaseHelper
import com.hzgc.util.common.ObjectUtil
import org.apache.hadoop.hbase.client.{Put, Table}
import org.apache.hadoop.hbase.util.Bytes

/**
  * 将数据保存至HBase
  */
object PutDataToHBase {
  /**
    * 保存聚类信息
    *
    * @param rowKey 年月（例如，2018-02）
    * @param list   聚类结果
    */
  def putClusteringInfo(rowKey: String, list: util.List[ClusteringAttribute]): Unit = {
    val ClusteringInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_ClUSTERINGINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.ClUSTERINGINFO_COLUMNFAMILY,
      ClusteringTable.ClUSTERINGINFO_COLUMN_YES,
      ObjectUtil.objectToByte(list))
    ClusteringInfo.put(put)
    ClusteringInfo.close()
  }

  /**
    *
    * @param rowKey 年月+类ID（例如，2018-02-1）
    * @param list   新增告警详细信息
    */
  def putDetailInfo(rowKey: String, list: util.List[AlarmInfo]): Unit = {
    val detailInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.DETAILINFO_COLUMNFAMILY,
      ClusteringTable.DETAILINFO_COLUMN_DATA,
      ObjectUtil.objectToByte(list))
    detailInfo.put(put)
    detailInfo.close()
  }

  /**
    *
    * @param rowKey 年月+类ID（例如，2018-02-1）
    * @param list   每条新增告警id
    */
  def putDetailInfo_v1(rowKey: String, list: List[Integer]): Unit = {
    val detailInfo: Table = HBaseHelper.getTable(ClusteringTable.TABLE_DETAILINFO)
    val put: Put = new Put(Bytes.toBytes(rowKey))
    put.addColumn(ClusteringTable.DETAILINFO_COLUMNFAMILY,
      ClusteringTable.DETAILINFO_COLUMN_DATA,
      ObjectUtil.objectToByte(list))
    detailInfo.put(put)
    detailInfo.close()
  }
}
