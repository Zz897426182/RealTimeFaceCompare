package com.hzgc.cluster.smallfile

import java.util

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode}

/**
  * 用于合并KafkaToParquet产生mid_table中的的临时小文件
  * A， 合并临时temp 目录下的文件
  * 1，遍历tmpTableHdfsPath 目录（临时目录），遍历出所有parquet 文件
  *   （即以.parquet 文件结尾的文件），得到对象files
  *（限制其每次读取的文件个数，防止程序因为小文件过多而崩溃}
  * 2，通过files，通过sparkSession 进行加载数据，得到df,并且存到一个临时目录中
  * 3, 查询加载数据中的时间和设备的对应关系，得到一个dateFrame 对象set01
  * 4, 遍历hisTableHdfsPath目录，最终表格的根目录，得到时间和设备对应关系set02
  * 5，对比set01 和set02,根据set01 中存在，set02 中不存在的数据，在最终的hive 表格中创建元数据
  * 6，通过api 把数据写入到最终的表格中
  * 7，删除原来的文件
  * 8，刷新表格的元数据
  *
  * 用法：
  * I, 配置如下两个文件。
  * resource 中的文件load-parquet-files.properties
  * 内容如下：（用来合并临时文件）
  * hdfs://hzgc
  * /user/hive/warehouse/mid_table
  * /user/hive/warehouse/person_table
  * person_table
  * 分别表示，参见MergeParquetFileV1的开始的参数验证
  *
  * II，启动如下脚本（做定时启动）
  * schema-merge-parquet-file.sh   (定时启动，合并临时文件，每2~5分钟启动一次)
  */
object MergeParquetFileV1 {
    val LOG = Logger.getLogger(MergeParquetFileV1.getClass)
    def main(args: Array[String]): Unit = {
        val start = System.currentTimeMillis
        if (args.length != 4) {
            System.out.print(s"""
                   |Usage: MergeParquetFile <hdfsClusterName> <tmpTableHdfsPath> <hisTableHdfsPath> <tableName> <dateString>
                   |<hdfsClusterName> 例如：hdfs://hacluster或者hdfs://hzgc
                   |<tmpTableHdfsPath> 临时表的根目录，需要是绝对路径
                   |<hisTableHdfsPath> 合并后的parquet文件，即总的或者历史文件的根目录，需要是绝对路径
                   |<tableName> 表格名字，最终保存的动态信息库的表格名字
                 """.stripMargin)
            System.exit(1)
        }
        // 接收传进来的四个参数
        val hdfsClusterName = args(0)
        val tmpTableHdfsPath = args(1)
        val hisTableHdfsPath = args(2)
        val tableName = args(3)

        // 初始化SparkSession，SparkSession 是单例模式的
        val sparkSession = SparkSessionSingleton.getInstance
        import sparkSession.sql
        // 根据sparkSession 得到SparkContext
        val sc = sparkSession.sparkContext
        // 设置hdfs 集群名字
        sc.hadoopConfiguration.set("fs.defaultFS", hdfsClusterName)
        // 获取hsfs 文件系统的实例
        val fs = FileSystem.get(sc.hadoopConfiguration)

        // 1，遍历tmpTableHdfsPath 目录（临时目录），遍历出所有parquet 文件
        //   （即以.parquet 文件结尾的文件），得到对象files
        val parquetFiles : util.ArrayList[String] = new util.ArrayList[String]()
        ReadWriteHDFS.getParquetFiles(new Path(tmpTableHdfsPath),fs, parquetFiles)

        val numOfParquetFiles = parquetFiles.size();
        if (numOfParquetFiles == 0) {
            LOG.info("there is no parquet files in mid_table, please check the streaming store application.")
            System.exit(1)
        }
        var pathArr : Array[String] = new Array[String](numOfParquetFiles)
        if (numOfParquetFiles >= 10000) {
            pathArr = new Array[String](10000)
        }
        var count = 0
        while (count < pathArr.length) {
            pathArr(count) = parquetFiles.get(count)
            count = count + 1
        }

        // 2，通过files，通过sparkSession 进行加载数据，得到df
        var personDF = sparkSession.read.parquet(pathArr : _*)
        personDF.persist()
        if (personDF.count() == 0) {
            LOG.info("there are parquet files, but no data in parquet files, just to delete the files.")
            // 删除临时表格中的文件
            ReadWriteHDFS.delV2(pathArr, fs);
            System.exit(2)
        }
        personDF.printSchema()

        // 3，查询加载数据中的时间和设备的对应关系，得到一个set01 对象
        personDF.createOrReplaceTempView("bigtmptable")
        val partitionsTmp = sql("select count(*),date,ipcid from bigtmptable group by date,ipcid")
        val partitionsTmpArray = partitionsTmp.select("date","ipcid").collect()

        // 4, 遍历hisTableHdfsPath目录，最终表格的根目录，得到时间和设备对应关系set02
        val personPartionsTmp = sql("show partitions " + tableName)
        val personPartionsArray = personPartionsTmp.collect()

        // 5，对比set01 和set02,根据set01 中存在，set02 中不存在的数据，在最终的hive 表格中创建元数据
        var i = 0
        while (i < partitionsTmpArray.length) {
            //date=2018-01-12/ipcid=DS-2CD2T20FD-I320160122AACH571485690
            val date = partitionsTmpArray(i).get(0)
            val ipcId = partitionsTmpArray(i).get(1)
            val partition = "date=" + date + "/ipcid=" + ipcId
            var j = 0
            var flag = 1
            while (j < personPartionsArray.length) {
                if (partition.equals(personPartionsArray(j).get(0))) {
                    flag = 0
                }
                j = j + 1
            }
            if (flag == 1) {
                sql("set hive.exec.dynamic.partition=true;")
                sql("set hive.exec.dynamic.partition.mode=nonstrict;")
                sql("alter table " + tableName + " add partition(date='" + date + "',ipcid='" + ipcId + "')")
            }
            i = i + 1
        }

        // 6, 根据加载的数据，进行分区，并且把数据存到Hive 的表格中,Hive 表格所处的根目录中
        personDF.coalesce(1)
            .write.mode(SaveMode.Append)
            .partitionBy("date", "ipcid")
            .parquet(hisTableHdfsPath)

        // 7,删除原来的文件
        // 删除临时表格上的文件
        ReadWriteHDFS.delV2(pathArr, fs)

        // 8, Reflesh spark store crash table data
        sql("REFRESH TABLE " + tmpTableHdfsPath.substring(tmpTableHdfsPath.lastIndexOf("/") + 1))
        sql("REFRESH TABLE " + tableName)

        sparkSession.close()

        LOG.info("总共花费的时间是： " + (System.currentTimeMillis() - start))
    }
}


