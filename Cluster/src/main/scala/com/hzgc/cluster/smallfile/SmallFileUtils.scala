package com.hzgc.cluster.smallfile

import org.apache.hadoop.fs.{ContentSummary, FileSystem, Path}

object SmallFileUtils {
    /** 根据输入目录计算目录大小，并以128*2M大小计算partition */
    def takePartition(src : String, fs : FileSystem) : Int = {
        val cos : ContentSummary = fs.getContentSummary(new Path(src))
        val sizeM : Long = cos.getLength/1024/1024
        val parNum : Int = sizeM/256 match {
            case 0 => 1
            case _ => (sizeM/256).toInt
        }
        parNum
    }
}
