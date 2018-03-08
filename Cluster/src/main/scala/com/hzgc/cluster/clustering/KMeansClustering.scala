package com.hzgc.cluster.clustering

import java.sql.Timestamp
import java.util
import java.util.{Calendar, Properties}

import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.dubbo.clustering.ClusteringAttribute
import org.apache.log4j.Logger
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

object KMeansClustering {

  case class Data(id: Long, time: Timestamp, ipc: String, host: String, spic: String, bpic: String)

  val LOG: Logger = Logger.getLogger(KMeansClustering.getClass)
  val numClusters = 50
  val numIterations = 100000
  var clusterIndex: Int = 0


  def main(args: Array[String]) {

    val driverClass = "com.mysql.jdbc.Driver"
    val sqlProper = new Properties()
    val properties = PropertiesUtils.getProperties
    val clusterNum = properties.getProperty("job.clustering.cluster.number")
    val iteraterNum = properties.getProperty("job.clustering.iterater.number")
    val appName = properties.getProperty("job.clustering.appName")
    val url = properties.getProperty("job.clustering.mysql.url")
    val tableName = properties.getProperty("job.clustering.mysql.table")
    val timeField = properties.getProperty("job.clustering.mysql.field.time")
    val ipcField = properties.getProperty("job.clustering.mysql.field.ipc")
    val dataField = properties.getProperty("job.clustering.mysql.field.data")
    val idField = properties.getProperty("job.clustering.mysql.field.id")
    val hostField = properties.getProperty("job.clustering.mysql.field.host")
    val spicField = properties.getProperty("job.clustering.mysql.field.spic")
    val bpicField = properties.getProperty("job.clustering.mysql.field.bpic")
    val partitionNum = properties.getProperty("job.clustering.partiton.number").toInt

    val spark = SparkSession.builder().appName(appName).enableHiveSupport().master("local[*]").getOrCreate()
    import spark.implicits._

    val calendar = Calendar.getInstance()
    val currentYearMon = "'" + calendar.get(Calendar.YEAR) + "-%" + (calendar.get(Calendar.MONTH) + 1) + "%'"

    spark.sql("select ftpurl,feature from person_table where date like " + currentYearMon).createOrReplaceTempView("parquetTable")

    val preSql = "(select T1.id, T2.host_name, T2.big_picture_url, T2.small_picture_url, T1.alarm_time " + "from t_alarm_record as T1 inner join t_alarm_record_extra as T2 on T1.id=T2.record_id " + "where T2.static_id IS NULL " + "and DATE_FORMAT(T1.alarm_time,'%Y-%m') like " + currentYearMon + ") as temp"

    sqlProper.setProperty("driver", driverClass)
    val dataSource = spark.read.jdbc(url, preSql, sqlProper)
    dataSource.printSchema()
    dataSource.map(data => {
      println("ftp://" + data.getAs[String](hostField) + ":2121/" + data.getAs[String](spicField))
      Data(data.getAs[Long](idField), data.getAs[Timestamp](timeField), data.getAs[String](spicField).substring(1, data.getAs[String](spicField).indexOf("/", 1)), data.getAs[String](hostField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](spicField), "ftp://" + data.getAs[String](hostField) + ":2121" + data.getAs[String](bpicField))
    }).createOrReplaceTempView("mysqlTable")

    val joinData = spark.sql("select T1.feature, T2.* from parquetTable as T1 inner join mysqlTable as T2 on T1.ftpurl=T2.spic")

    val idPointRDD = joinData.rdd.map(data => (data.getAs[String]("spic"), Vectors.dense(data.getAs[mutable.WrappedArray[Float]]("feature").toArray.map(_.toDouble)))).cache()
    val kMeansModel = KMeans.train(idPointRDD.map(_._2).sample(withReplacement = false, 0.4), numClusters, numIterations)
    val trainMidResult = kMeansModel.predict(idPointRDD.map(_._2))
    // TODO: 删除 map(data => (data._1, data._2.toList.sortWith((a, b) => a.getTimestamp(1).getTime > b.getTimestamp(1).getTime)))
    var trainResult = trainMidResult.zip(joinData.select("id", "time", "ipc", "host", "spic", "bpic").rdd).groupByKey().map(data => (data._1, data._2.toList.sortWith((a, b) => a.getTimestamp(1).getTime > b.getTimestamp(1).getTime))).map(data => (data._1, data._2.toArray.sortWith((a, b) => a.getTimestamp(1).getTime > b.getTimestamp(1).getTime)))

    val table1List = new util.ArrayList[ClusteringAttribute]()
    trainResult.map(data => {
      val attribute = new ClusteringAttribute()
      attribute.setClusteringId(data._1.toString)
      attribute.setCount(data._2.length)
      attribute.setFirstAppearTime(data._2(0).getTimestamp(1).toString)
      attribute.setFirstIpcId(data._2(0).getAs[String]("ipc"))
      attribute.setLastAppearTime(data._2(data._2.length - 1).getTimestamp(1).toString)
      attribute.setLastIpcId(data._2(data._2.length - 1).getAs[String]("ipc"))
      attribute.setFtpUrl(data._2(data._2.length / 2).getAs[String]("spic"))
      attribute
    }).collect().foreach(data => table1List.add(data))

    val mon = calendar.get(Calendar.MONTH) + 1
    var monStr = ""
    if (mon < 10) {
      monStr = "0" + mon
    } else {
      monStr = String.valueOf(mon)
    }
    val yearMon = calendar.get(Calendar.YEAR) + "-" + monStr
    LOG.info("write clustering info to HBase...")
    PutDataToHBase.putClusteringInfo(yearMon, table1List)

    trainResult.foreach(data => {
      val rowKey = yearMon + "-" + data._1
      println(rowKey)
      val idList = new util.ArrayList[Integer]()
      data._2.foreach(data => idList.add(data.getAs[Long]("id").toInt))
      println(idList)
      println("++++++++++++++++++++++")
      PutDataToHBase.putDetailInfo_v1(rowKey, idList)
    })
    spark.stop()
  }
}

