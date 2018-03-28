package com.hzgc.cluster.smallfile

import java.util

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode}

/**
  * 概述：
  *     最终生成的parquet文件，存在的目录大致如下：
  *     /user/hive/warehouse/person_table/date=2018-01-12/ipcid=3K01E84PAU00083
  *     /user/hive/warehouse/person_table/date=2018-01-12/ipcid=DS-2CD2T20FD-I320160122AACH571485690
  *     这样只要进入到每个目录，进行分别处理，每个摄像头的数据分别处理，而不是把一整天的数据
  *     一下子读取进来。
  */
object MergeParquetFileV2 {
    val LOG = Logger.getLogger(MergeParquetFileV2.getClass)

    def main(args: Array[String]): Unit = {
        if (args.length != 5) {
            System.out.print(
                s"""
                   |Usage: MergeParquetFile <hdfsClusterName> <tmpTableHdfsPath> <hisTableHdfsPath> <tableName> <dateString>
                   |<hdfsClusterName> 例如：hdfs://hacluster或者hdfs://hzgc
                   |<tmpTableHdfsPath> 临时表的根目录，需要是绝对路径
                   |<hisTableHdfsPath> 合并后的parquet文件，即总的或者历史文件的根目录，需要是绝对路径
                   |<tableName> 表格名字，最终保存的动态信息库的表格名字
                   |<dateString > 用于表示需要合并的某天的内容
                 """.stripMargin)
            System.exit(1)
        }
        // 接收传进来的四个或者五个个参数
        val hdfsClusterName = args(0)
        val tmpTableHdfsPath = args(1)
        val hisTableHdfsPath = args(2)
        val tableName = args(3)
        val dateString = args(4)

        // 初始化SparkSession，SparkSession 是单例模式的
        val sparkSession = SparkSessionSingleton.getInstance
        import sparkSession.sql
        // 根据sparkSession 得到SparkContext
        val sc = sparkSession.sparkContext
        // 设置hdfs 集群名字
        sc.hadoopConfiguration.set("fs.defaultFS", hdfsClusterName)
        // 获取hsfs 文件系统的实例
        val fs = FileSystem.get(sc.hadoopConfiguration)
        //获取person_table 每天产生的目录中的最后一级目录的绝对路径
        val personTableFinalDirSet: util.Set[String] = new util.HashSet[String]()
        ReadWriteHDFS.getPersonTableFinalDir(new Path(hisTableHdfsPath), fs, personTableFinalDirSet);

        // 用于遍历每天每个摄像头的绝对路径
        val personTableFinalDirSetIt = personTableFinalDirSet.iterator();
        while(personTableFinalDirSetIt.hasNext) {
            // 最终的parquest 存放的目录的绝对路径
            // 例如/user/hive/warehouse/person_table/date=2018-01-12/ipcid=3K01E84PAU00083
            var parquetFileDir = personTableFinalDirSetIt.next();
            // 用于保存遍历出来的文件
            var parquetFiles: util.ArrayList[String] = new util.ArrayList[String]()
            // 获取/user/hive/warehouse/person_table/date=2018-01-12/ipcid=3K01E84PAU00083 下的parquest 文件
            ReadWriteHDFS.getParquetFilesV2(dateString, new Path(parquetFileDir), fs, parquetFiles)
            val numOfFiles = parquetFiles.size();
            // 把parquet 文件的list 转换成数组
            var pathArr : Array[String] = new Array[String](numOfFiles)
            // 如果里面没有文件或者文件个数为1，直接跳过
            if (numOfFiles != 0 && numOfFiles != 1) {
                var count = 0
                while (count < pathArr.length) {
                    pathArr(count) = parquetFiles.get(count)
                    count = count + 1
                }
                var personDF = sparkSession.read.parquet(pathArr : _*)
                // 保存文件
                personDF.coalesce(1).repartition(SmallFileUtils.takePartition(parquetFileDir, fs))
                    .write.mode(SaveMode.Append).parquet(parquetFileDir)
                // 删除已经被合并的文件
                ReadWriteHDFS.del(pathArr, fs)
            }
        }

        sql("REFRESH TABLE " + tableName)

        sparkSession.close()
    }
}


