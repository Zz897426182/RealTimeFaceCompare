package com.hzgc.scala.job.streaming

import com.hzgc.java.util.PropertiesUtils
import com.hzgc.jni.{NativeFunction, FaceFunction}
import com.hzgc.scala.util.{ComparisonDataUtils, ContextInitUtils}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.{Durations, StreamingContext}

/**
 * 人脸识别告警任务
 * 1、本任务为识别告警实现任务。
 * 2、本任务为实时处理任务。
 * 3、本任务数据处理大概流程为：
 *    kafka（集群）--》sparkStreaming--》Hbase
 */
object FaceRecognizeAlarmJob {

  def main(args: Array[String]) {

    //初始化streaming上下文
    val conf = new SparkConf().
      setAppName(PropertiesUtils.getPropertiesValue("job.recognizeAlarm.appName")).
      setMaster(PropertiesUtils.getPropertiesValue("job.recognizeAlarm.master"))
    val sc = new SparkContext(conf)
    //初始化sqlContext
    val sqlContext = new SQLContext(sc)
    //初始化StreamingContext
    val ssc = new StreamingContext(sc,Durations.seconds(3))

    //获取kafka集群的动态人脸照片
    val photoDStream = ComparisonDataUtils.getKafkaDynamicPhoto(ssc)
    //提取特征值
    //算法初始化
    NativeFunction.init()
    val eatureExtract = photoDStream.map(a =>(a._1,FaceFunction.featureExtract(a._2)))
    eatureExtract.print()
    //将floatArray类型的特征值转化为string,同时过滤特征值为空的元素
    val eature2string = eatureExtract.map(b =>(b._1,FaceFunction.floatArray2string(b._2))).filter(null != _._2)
    eature2string.print()


    ssc.start()
    ssc.awaitTermination()




  }

}
