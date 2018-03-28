package com.hzgc.cluster.loaddata

import java.io.File
import java.util

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession

/**
  * 功能：LoadParquetFile工具类
  *       1.通过遍历hdfs上/user/hive/warehouse/person_table/目录下所有的parquet文件并获取文件路径
  *       2.从所有文件的路径中解析出分区字段date和ipcid
  *       3.设置hive表动态分区属性，并向person_table表中添加不存在的分区
  *       4.分区加载完毕刷新表
  * 要求：core-site.xml 和hdfs-site.xml 和hive-site.xml 需要放在resources 目录下，最终需要放到conf 目录下
  * 用法：1.配置文件
  *       resources目录下的load-parquet-files.properties文件内容如下
  *       hdfs://hzgc
  *      /user/hive/warehouse/person_table
  *       person_table
  *       2.执行脚本
  *       load-parquet-files.sh
  */
object LoadParquetFile {
  val LOG = Logger.getLogger(LoadParquetFile.getClass)
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN) //设置输出级别为Warn级别

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      System.out.print(
        s"""
           |Usage: LoadParquetFile <hdfsClusterName> <hdfsPath> <tableName>
           |<hdfsClusterName> 例如：hdfs://hacluster或者hdfs://hzgc
           |<hdfsPath> 数据存放的根目录 例如:/user/hive/warehouse/person_table
           |<tableName> 表格名字，最终保存的动态信息库的表格名字
        """.stripMargin)
      System.exit(1)
    }
    //接收传来的三个参数
    val hdfsClusterName = args(0)
    val hdfsPath = args(1)
    val tableName = args(2)

    // 初始化SparkSession，SparkSession 是单例模式的
    val sparkSession = SparkSessionSingleton.getInstance()
    import sparkSession.sql
    // 根据sparkSession 得到SparkContext
    val sc = sparkSession.sparkContext
    // 设置hdfs 集群名字
    sc.hadoopConfiguration.set("fs.defaultFS", hdfsClusterName)
    // 获取hsfs 文件系统的实例
    val fs = FileSystem.get(sc.hadoopConfiguration)
    import scala.collection.JavaConversions._
    val parquetFiles: util.ArrayList[String] = new util.ArrayList[String]()
    ReadHDFS.listFiles(new Path(hdfsPath), fs, parquetFiles)
    if (parquetFiles != null && parquetFiles.size() > 0) {
      for (parquetFile <- parquetFiles) {
        val date = parquetFile.substring(parquetFile.indexOf("=") + 1, parquetFile.lastIndexOf("/"))
        val ipcId = parquetFile.substring(parquetFile.lastIndexOf("=") + 1)
        LOG.warn("Hive metadata updated partition is :"+date+"  Ipcid is :"+ipcId+" success")
        sql("set hive.exec.dynamic.partition=true;")
        sql("set hive.exec.dynamic.partition.mode=nonstrict;")
        sql("alter table " + tableName + " add if not exists partition(date='" + date + "',ipcid='" + ipcId + "')")
      }
      sql("REFRESH TABLE " + tableName)
      LOG.warn(" *********** UpData Done **********")
    } else {
      LOG.warn("parquetFiles is null or parquetFiles size is 0")
    }
  }
}

/**
  * 有关HDFS 的操作
  */
object ReadHDFS {
  /**
    * 循环遍历根目录path下的parquet 文件，并获取路径
    *
    * @param hdfsFilePath 数据在hdfs上根目录
    * @param fs           hdfs文件系统
    * @param files        获取到的路径存放到集合
    */
  def listFiles(hdfsFilePath: Path, fs: FileSystem, files: util.ArrayList[String]) {

    val fstats = fs.listStatus(hdfsFilePath)
    for (fstat <- fstats) {
      if (fstat.isDirectory) {
        listFiles(fstat.getPath, fs, files)
        if (fstat.getPath.toString.contains("ipcid")) {
          files.add(fstat.getPath.toString)
        }
      }
    }
  }
}

/**
  * 保持SparkSession 是单例模式的
  */
object SparkSessionSingleton {
  @transient private var instance: SparkSession = _
  val warehouseLocation = new File("spark-warehouse").getAbsolutePath

  def getInstance(): SparkSession = {
    if (instance == null) {
      instance = SparkSession.builder()
        .appName("load-parquest-demo")
        //.master("local[*]")
        .config("spark.sql.parquet.compression.codec", "snappy")
        .config("spark.sql.warehouse.dir", warehouseLocation)
        .enableHiveSupport()
        .getOrCreate()
    }
    instance
  }
}
