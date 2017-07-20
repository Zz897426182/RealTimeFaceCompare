package com.hzgc.scala.util

import com.hzgc.java.util.PropertiesUtils
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils


object ComparisonDataUtils {

  /**
   * SparkStreaming从kafka集群读取photo数据
   * @param ssc streaming上下文环境
   * @return 返回InputDStream
   */
  def getKafkaDynamicPhoto(ssc :StreamingContext): InputDStream[Tuple2[String, Array[Byte]]] ={
    val topics = Set(PropertiesUtils.getPropertiesValue("kafka.topic.name"))
    val brokers = PropertiesUtils.getPropertiesValue("kafka.metadata.broker.list")
    val groupId = PropertiesUtils.getPropertiesValue("kafka.group.id")
    val kafkaParams = Map(
    "metadata.broker.list" -> brokers,
    "group.id" ->groupId
    )
    val kafkainput = KafkaUtils.createDirectStream[String, Array[Byte],StringDecoder, DefaultDecoder](ssc, kafkaParams, topics)
    kafkainput
  }

  /**
   * 读取静态信息库的数据
   * @param sc spark上下文环境
   * @return RDD[(ImmutableBytesWritable,Result)]
   */
  def getHbaseStaticPhoto(sc:SparkContext): RDD[(ImmutableBytesWritable,Result)] ={
    val hbaseConf = HBaseConfiguration.create()
    val tableName = PropertiesUtils.getPropertiesValue("hbase.table.name")
    hbaseConf.set("hbase.zookeeper.quorum",PropertiesUtils.getPropertiesValue("hbase.zookeeper.quorum"))
    hbaseConf.set("hbase.zookeeper.property.clientPort",PropertiesUtils.getPropertiesValue("hbase.zookeeper.property.clientPort"))
    hbaseConf.set(TableInputFormat.INPUT_TABLE,tableName)
    val rdd = sc.newAPIHadoopRDD(hbaseConf,classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])
   rdd
  }
  def main(args: Array[String]) {
   // val scc =ContextInitUtils.getStreamingContext("demo","local[*]",3)
    //获取kafka的inputDStream
    val sc = ContextInitUtils.getSparkContext("aaaaaa","local[8]")
    val result = getHbaseStaticPhoto(sc)
    val ss=result.map(a =>{
      val k =   Bytes.toString(a._1.get())
      val v =a._2.getValue("t1".getBytes,"p".getBytes)
      (k,v)
    })
    ss.foreach(println)
    val count = result.count()
    println(count)


  }


}
