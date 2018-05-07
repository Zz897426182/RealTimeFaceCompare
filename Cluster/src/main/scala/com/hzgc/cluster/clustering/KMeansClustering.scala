package com.hzgc.cluster.clustering

import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import java.text.SimpleDateFormat
import java.{sql, util}
import java.util.{Calendar, Date, Properties, UUID}

import com.hzgc.cluster.consumer.PutDataToEs
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.collect.expand.util.FTPDownloadUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object KMeansClustering {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  case class CenterData(clusterID: Int, featureData: Array[Double])

  //exception when write to es
  System.setProperty("es.set.netty.runtime.available.processors", "false")
  val LOG: Logger = Logger.getLogger(KMeansClustering.getClass)

  def main(args: Array[String]) {
    val driverClass = "com.mysql.jdbc.Driver"
    val sqlProper = new Properties()
    val properties = PropertiesUtils.getProperties
    val clusterNum = properties.getProperty("job.clustering.cluster.number")
    val similarityThreshold = properties.getProperty("job.clustering.similarity.Threshold").toDouble
    val center_similarityThreshold = properties.getProperty("job.clustering.similarity.center.Threshold").toDouble
    val appearCount = properties.getProperty("job.clustering.appear.count").toInt
    val iteratorNum = properties.getProperty("job.clustering.iterator.number")
    val appName = properties.getProperty("job.clustering.appName")
    val url = properties.getProperty("job.clustering.mysql.url")
    val alarmTableName = properties.getProperty("job.clustering.mysql.alarm.record.table")
    val alarmExtraTableName = properties.getProperty("job.clustering.mysql.alarm.record.extra.table")
    val timeField = properties.getProperty("job.clustering.mysql.field.time")
    val idField = properties.getProperty("job.clustering.mysql.field.id")
    val hostField = properties.getProperty("job.clustering.mysql.field.host")
    val spicField = properties.getProperty("job.clustering.mysql.field.spic")
    val bpicField = properties.getProperty("job.clustering.mysql.field.bpic")
    //val partitionNum = properties.getProperty("job.clustering.partition.number").toInt
    val month_temp = properties.getProperty("job.clustering.month")
    val capture_url = properties.getProperty("job.clustering.capture.database.url")
    val capture_data_table = properties.getProperty("job.clustering.capture.data")
    val capture_track_table = properties.getProperty("job.clustering.capture.track")
    val capture_data_table_user = properties.getProperty("job.clustering.capture.database.user")
    val capture_data_table_password = properties.getProperty("job.clustering.capture.database.password")
    val spark = SparkSession.builder().appName(appName).enableHiveSupport().getOrCreate()
    import spark.implicits._

    //set calendar
    val calendar = Calendar.getInstance()
    val mon = if (month_temp != null) month_temp.toInt else calendar.get(Calendar.MONTH)
    var monStr = ""
    if (mon < 10) {
      monStr = "0" + mon
    } else {
      monStr = String.valueOf(mon)
    }
    val yearMon = calendar.get(Calendar.YEAR) + "-" + monStr
    val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + mon + "%'"

    //get parquet data
    spark.sql("select distinct ftpurl,feature from person_table where date like " + currentYearMon).createOrReplaceTempView("parquetTable")
    val parquetDataCount = spark.sql("select ftpurl from parquetTable").count()
    LOG.info("parquet data count :" + parquetDataCount)

    //get alarm data from mysql
    val preSql = "(select T1.id, T2.host_name, T2.big_picture_url, T2.small_picture_url, T1.alarm_time from  " + alarmTableName + "  as T1 inner join " + alarmExtraTableName + "  as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as alarm_record"
    sqlProper.setProperty("driver", driverClass)
    val dataSource = spark.read.jdbc(url, preSql, sqlProper)
    val mysqlDataCount = dataSource.count()
    LOG.info("mysql data count :" + mysqlDataCount)

    if (parquetDataCount > 0 && mysqlDataCount > 0) {
      dataSource.map(data => {
        Data(data.getAs[Long](idField), data.getAs[Timestamp](timeField), data.getAs[String](spicField).substring(1, data.getAs[String](spicField).indexOf("/", 1)), data.getAs[String](hostField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](bpicField))
      }).createOrReplaceTempView("mysqlTable")

      //get the region and ipcIdList
      val region_Ipc_sql = "(select T1.region_id,GROUP_CONCAT(T2.serial_number) " + "as serial_numbers from t_region_department as T1 inner join " + "(select concat(dep.parent_ids,',',dep.id) as path ,T3.serial_number from " + "t_device as dev left join t_department as dep on dev.department_id = dep.id inner join " + "t_device_extra as T3 on dev.id = T3.device_id ) as T2 on T2.path " + "like concat('%',T1.department_id,'%') group by T1.region_id " + "order by T1.region_id,T2.serial_number ) as test"
      val region_Ipc_data = spark.read.jdbc(url, region_Ipc_sql, sqlProper).collect()
      val region_IpcMap = mutable.HashMap[Int, String]()
      region_Ipc_data.foreach(data => region_IpcMap.put(data.getAs[Int](0), data.getAs[String](1)))

      //loops for each region
      for (region_Ipc <- region_IpcMap) {
        val uuidString = UUID.randomUUID().toString
        val region = region_Ipc._1
        val ipcList = region_Ipc._2.split(",")
        var ipcStr = ""
        for (j <- 0 until ipcList.length) {
          if (j != ipcList.length - 1) {
            ipcStr += "'" + ipcList(j) + "'" + ","
          } else {
            ipcStr += "'" + ipcList(j) + "'"
          }
        }
        var finalStr = ""
        finalStr += "(" + ipcStr + ")"
        LOG.info("ipcIdList of region: " + region + " is: " + finalStr)
        //get data from parquet join mysql
        val joinData = spark.sql("select T1.feature,T2.* from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic where T2.ipc in " + finalStr).cache()
        val ftpUtl_Feature = joinData.rdd.map(data => (data.getAs[String]("spic"), Vectors.dense(data.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble)))).cache()
        val dataCount = ftpUtl_Feature.count()
        LOG.info("join data count is: " + dataCount)

        //train the model
        val numClusters = if (clusterNum == null || "".equals(clusterNum)) Math.sqrt(dataCount.toDouble).toInt else clusterNum.toInt
        val maxIterations = if (iteratorNum == null || "".equals(iteratorNum)) 10000 else iteratorNum.toInt
        val kMeansModel = KMeans.train(ftpUtl_Feature.map(data => data._2), numClusters, maxIterations)

        //predict each point belong to which clustering center and filter by similarityThreshold
        val label_Center_Feature = ftpUtl_Feature.map(_._2).map(p => (kMeansModel.predict(p), kMeansModel.clusterCenters.apply(kMeansModel.predict(p)), p))
        val point_center_dist = label_Center_Feature.map(data => (data._1, cosineMeasure(data._2.toArray, data._3.toArray)))
        val viewData = joinData.select("id", "time", "ipc", "host", "spic", "bpic", "feature").rdd
        val predictResult = point_center_dist.zip(viewData).distinct().groupBy(key => key._1._1).mapValues(f => {
          f.toList.filter(data => data._1._2 > similarityThreshold).sortWith((a, b) => a._1._2 > b._1._2)
        }).filter(data => data._2.nonEmpty)

        val keyList = predictResult.map(data => data._1).collect().toList
        //get the most similarity point of each clustering and add to topSimDataList
        val topPoint_center = predictResult.map(data => (data._1, data._2.head._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble)))
        val topSimDataList = new util.ArrayList[CenterData]()
        topPoint_center.collect().foreach(x => {
          val centerData = CenterData(x._1, x._2)
          topSimDataList.add(centerData)
        })

        //compare each two center points and merge it when the similarity is larger than the threshold
        val topSimDataListCopy = new util.ArrayList[CenterData]()
        val deleteCenter = new util.ArrayList[Int]()
        val union_center = new util.HashMap[Int, ArrayBuffer[Int]]
        topSimDataListCopy.addAll(topSimDataList)
        for (k <- 0 until topSimDataListCopy.size()) {
          val first = topSimDataListCopy.get(k)
          if (!deleteCenter.contains(first.clusterID)) {
            val centerSimilarity = ArrayBuffer[Int]()
            val iter = topSimDataList.iterator()
            while (iter.hasNext) {
              val second = iter.next()
              val pairSim = cosineMeasure(first.featureData, second.featureData)
              if (pairSim > center_similarityThreshold) {
                deleteCenter.add(second.clusterID)
                centerSimilarity += second.clusterID
                iter.remove()
              }
            }
            union_center.put(first.clusterID, centerSimilarity)
          }
        }

        //union similarity clustering
        predictResult.map(data => (data._1, data._2)).sortByKey()
        var indexedResult = IndexedRDD(predictResult).cache()
        val iter_center = union_center.keySet().iterator()
        //var indexed1 = indexedResult
        while (iter_center.hasNext) {
          val key = iter_center.next()
          if (keyList.contains(key)) {
            val value = union_center.get(key)
            if (value.length > 1) {
              val first_list_option = indexedResult.get(key).orNull
              if (first_list_option != null && first_list_option.size > 1) {
                for (i <- 1 until value.length) {
                  val first_list = first_list_option
                  val cluster_tmp = value(i)
                  val arrayBuffer = ArrayBuffer[Int]()
                  val second_list = indexedResult.get(cluster_tmp).orNull
                  if (second_list != null && second_list.size > 1) {
                    val topSim = cosineMeasure(first_list.head._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble), second_list.apply(1)._2.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble))
                    if (topSim > center_similarityThreshold) {
                      indexedResult = indexedResult.put(key, first_list.union(second_list.drop(0)))
                      arrayBuffer += cluster_tmp
                    }
                  }
                  indexedResult = indexedResult.delete(arrayBuffer.toArray)
                }
              }
            }
          }
        }
        //filter clustering by appear times
        calendar.set(Calendar.MONTH, mon - 1)
        val totalDay = calendar.getActualMaximum(Calendar.DATE)
        val lastResult = indexedResult.map(data => {
          val dateArr = new Array[Int](totalDay)
          val dateList = new util.ArrayList[Int](data._2.length)
          data._2.foreach(data => {
            val date = data._2.getAs[Timestamp]("time").toLocalDateTime.getDayOfMonth
            dateList.add(date)
          })
          dateList.toArray().distinct.foreach(data => {
            dateArr(data.asInstanceOf[Int] - 1) = 1
          })
          (data, maxAppearTimes(dateArr))
        })

        //put all the clustering data to HBase
        val table1List = new util.ArrayList[ClusteringAttribute]()
        val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val finalData = lastResult.filter(data => data._2 >= appearCount).map(data => data._1).map(data => (data._1, data._2.toArray.sortWith((a, b) => a._2.getAs[Timestamp]("time").toLocalDateTime.toString > b._2.getAs[Timestamp]("time").toLocalDateTime.toString)))
        var conn: Connection = null
        var pst: PreparedStatement = null
        var blobSmall: sql.Blob = null
        var blobBig: sql.Blob = null
        finalData.map(f = data => {
          conn = DriverManager.getConnection(capture_url, capture_data_table_user, capture_data_table_password)
          val attribute = new ClusteringAttribute()
          val clusterId = region + "-" + data._1.toString + "-" + uuidString
          val smallPic = FTPDownloadUtils.downloadftpFile2Bytes(data._2.head._2.getAs[String]("spic"))
          val bigPic = FTPDownloadUtils.downloadftpFile2Bytes(data._2.head._2.getAs[String]("bpic"))
          val insertDataSql = "insert into " + capture_data_table + "(id,update_time,small_picture,big_picture,camera_code,capture_person_type) values (?,?,?,?,?,?)"
          println("+++++++++++" + insertDataSql)
          try {
            pst = conn.prepareStatement(insertDataSql)
            pst.setString(1, clusterId)
            pst.setTimestamp(2, data._2.head._2.getTimestamp(1))
            blobSmall = conn.createBlob()
            blobSmall.setBytes(1, smallPic)
            pst.setBlob(3, blobSmall)
            blobBig = conn.createBlob()
            blobBig.setBytes(1, bigPic)
            pst.setBlob(4, blobBig)
            pst.setString(5, data._2.head._2.getAs[String]("ipc"))
            pst.setString(6,"0002")
            pst.executeUpdate()
            LOG.info("put data to t_capture_data successful")
          } catch {
            case e: Exception => LOG.info("put data to t_capture_data failed=======" + e.getMessage)
          }
          if (pst != null) {
            pst.close()
          }
          if (conn != null) {
            conn.close()
          }
          attribute.setClusteringId(clusterId)
          attribute.setCount(data._2.length)
          attribute.setLastAppearTime(sdf.format(data._2.head._2.getTimestamp(1)))
          attribute.setLastIpcId(data._2.head._2.getAs[String]("ipc"))
          attribute.setFirstAppearTime(sdf.format(data._2.last._2.getTimestamp(1)))
          attribute.setFirstIpcId(data._2.last._2.getAs[String]("ipc"))
          attribute.setFtpUrl(data._2.head._2.getAs[String]("spic"))
          attribute
        }).collect().foreach(data => table1List.add(data))

        //put all cluster to HBase
        val rowKey = yearMon + "-" + region
        LOG.info("write clustering info to HBase...")
        PutDataToHBase.putClusteringInfo(rowKey, table1List)

        //update each clustering data to es
        val putDataToEs = PutDataToEs.getInstance()
        finalData.foreachPartition(part => {
          conn = DriverManager.getConnection(capture_url, capture_data_table_user, capture_data_table_password)
          part.foreach(data => {
            val rowKey = yearMon + "-" + region + "-" + data._1 + "-" + uuidString
            val clusterId = rowKey + "-" + data._1 + "-" + uuidString
            LOG.info("the current clusterId is:" + clusterId)
            val insertSql = "insert into " + capture_track_table + "(id,update_time,camera_code) values (?,?,?)"
            pst = conn.prepareStatement(insertSql)
            data._2.foreach(p => {
              val date = new Date(p._2.getAs[Timestamp]("time").getTime)
              val dateNew = sdf.format(date)
              val status = putDataToEs.upDateDataToEs(p._2.getAs[String]("spic"), rowKey, dateNew, p._2.getAs[Long]("id").toInt)
              if (status != 200) {
                LOG.info("Put data to es failed! The ftpUrl is " + p._2.getAs("spic"))
              }
              try {
                val uuidStr = UUID.randomUUID().toString
                pst.setString(1, uuidStr)
                pst.setTimestamp(2, p._2.getAs[Timestamp]("time"))
                pst.setString(3, p._2.getAs[String]("ipc"))
                pst.executeUpdate()
                LOG.info("put data to t_capture_track successful")
              } catch {
                case e: Exception => LOG.info("put data to t_capture_track failed=======:" + e.getMessage)
              }
            })
          })
          if (pst != null) {
            pst.close()
          }
          if (conn != null) {
            conn.close()
          }
        })
      }
    } else {
      LOG.info("no data read from parquet or mysql")
    }
    spark.stop()
  }

  /**
    * cosine similarity calculate
    *
    * @param v1 feature Array 1
    * @param v2 feature Array 2
    * @return similarity
    */
  def cosineMeasure(v1: Array[Double], v2: Array[Double]): Double = {

    val member = v1.zip(v2).map(d => d._1 * d._2).sum
    //求出分母第一个变量值
    val temp1 = math.sqrt(v1.map(num => {
      math.pow(num, 2)
    }).sum)
    //求出分母第二个变量值
    val temp2 = math.sqrt(v2.map(num => {
      math.pow(num, 2)
    }).sum)
    //求出分母
    val denominator = temp1 * temp2
    //进行计算
    0.5 + 0.5 * (member / denominator)
  }

  /**
    * calculator the max appear times of each cluster
    *
    * @param dateArr appear date array of the cluster
    * @return max appearence times
    */
  def maxAppearTimes(dateArr: Array[Int]): Int = {
    var count = 0
    var temp = 0
    for (n <- dateArr) {
      if (n == 1) {
        temp += 1
      } else {
        temp = 0
      }
      count = if (count > temp) count else temp
    }
    count
  }
}


