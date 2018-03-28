package com.hzgc.cluster.smallfile

import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}


/**
  * 有关HDFS 的操作
  */
object ReadWriteHDFS {

    /**
      * 删除文件，ture 表示迭代删除目录或者文件
      *
      * @param paths 文件列表
      * @param fs    hdfs文件系统实例
      */
    def del(paths: Array[String], fs: FileSystem): Unit = {
        for (f <- paths) {
            fs.delete(new Path(f), true)
        }
    }

    /**
      * 删除文件，ture 表示迭代删除目录或者文件
      *
      * @param paths 文件列表
      * @param fs    hdfs文件系统实例
      */
    def delV2(paths: Array[String], fs: FileSystem): Unit = {
        for (f <- paths) {
            fs.delete(new Path(f.substring(0, f.lastIndexOf("/"))), true)
        }
    }

    /**
      * 循环遍历根目录path下的parquet 文件,
      *
      * @param path  根目录path
      * @param fs    hdfs 文件系统对象
      * @param files 最终保存的文件
      */
    def getParquetFiles(path: Path, fs: FileSystem, files: java.util.ArrayList[String]) {
        if (fs != null && path != null) {
            val fileStatusArr = fs.listStatus(path)
            for (fileStatus <- fileStatusArr) {
                if (fileStatus.isDirectory()) {
                    getParquetFiles(fileStatus.getPath, fs, files)
                } else if (fileStatus.isFile && fileStatus.getPath.toString.endsWith(".parquet") &&
                    !fileStatus.getPath.toString.contains("_temporary/")) {
                    files.add(fileStatus.getPath.toString)
                }
            }
        }
    }

    /**
      * 根据时间循环遍历根目录path下的parquet 文件,
      *
      * @param dateString 时间
      * @param path       根目录path
      * @param fs         hdfs 文件系统对象
      * @param files      最终保存的文件
      */
    def getParquetFiles(dateString: String, path: Path, fs: FileSystem, files: java.util.ArrayList[String]) {
        if (fs != null && path != null) {
            val fileStatusArr = fs.listStatus(path)
            for (fileStatus <- fileStatusArr) {
                val finalPathString = fileStatus.getPath.toString
                if (fileStatus.isDirectory()) {
                    getParquetFiles(dateString, fileStatus.getPath, fs, files)
                } else if (fileStatus.isFile && finalPathString.endsWith(".parquet")
                    && finalPathString.contains(dateString) && !fileStatus.getPath.toString.contains("_temporary/")) {
                    files.add(finalPathString)
                }
            }
        }
    }

    /**
      * 根据时间循环遍历根目录path下的parquet 文件,
      *
      * @param dateString 时间
      * @param path       根目录path
      * @param fs         hdfs 文件系统对象
      * @param files      最终保存的文件
      */
    def getParquetFilesV2(dateString: String, path: Path, fs: FileSystem, files: java.util.ArrayList[String]) {
        if (fs != null && path != null) {
            val fileStatusArr = fs.listStatus(path)
            for (fileStatus <- fileStatusArr) {
                val finalPathString = fileStatus.getPath.toString
                if (fileStatus.isDirectory()) {
                    getParquetFiles(dateString, fileStatus.getPath, fs, files)
                } else if (fileStatus.isFile && finalPathString.endsWith(".parquet")
                    && finalPathString.contains(dateString) && !fileStatus.getPath.toString.contains("_temporary/")) {
                    val cos : ContentSummary = fs.getContentSummary(new Path(finalPathString))
                    val sizeM : Long = cos.getLength/1024/1024
                    if (sizeM > 200 && sizeM < 300) {

                    } else {
                        files.add(finalPathString)
                    }
                }
            }
        }
    }

    /**
      * 取得时间和设备的对应的分区
      *
      * @param src        根目录
      * @param fs         hdfs 文件系统对象
      * @param partitions 用于保存时间和ipcid设备编号的对应关系
      */
    def getPartitions(src: Path, fs: FileSystem, partitions: java.util.Set[String]) {
        if (fs != null && src != null) {
            val fileStatusArr = fs.listStatus(src)
            for (fileStatus <- fileStatusArr) {
                if (fileStatus.isDirectory()) {
                    val path = fileStatus.getPath.toString
                    if (path.contains("ipcid"))
                        partitions.add(path.substring(path.indexOf("date=")))
                    else
                        getPartitions(fileStatus.getPath, fs, partitions)
                } else {

                }
            }
        }
    }

    /**
      * 取得时间和设备的对应的分区
      *
      * @param src        根目录
      * @param fs         hdfs 文件系统对象
      * @param personTableFinalDirSet 用于保存时person_table 每天的产生的目录中的最后一级目录
      */
    def getPersonTableFinalDir(src: Path, fs: FileSystem, personTableFinalDirSet: java.util.Set[String]) {
        if (fs != null && src != null) {
            val fileStatusArr = fs.listStatus(src)
            for (fileStatus <- fileStatusArr) {
                if (fileStatus.isDirectory()) {
                    val path = fileStatus.getPath.toString
                    if (path.contains("ipcid")) {
                        personTableFinalDirSet.add(path)
                    }
                    else {
                        getPersonTableFinalDir(fileStatus.getPath, fs, personTableFinalDirSet)
                    }
                } else {

                }
            }
        }
    }
}

