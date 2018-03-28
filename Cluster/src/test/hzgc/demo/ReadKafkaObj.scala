package demo

/**
  * spark streaming 使用解码器StudentDecoder读取kafka里面写入的对象进行处理。
  */

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

object ReadKafkaObj {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("demo").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Durations.seconds(3))
    val kafkaParams = Map(
      "metadata.broker.list" -> "172.18.18.100:21005,172.18.18.101:21005,172.18.18.102:21005",
      "group.id" -> "1234"
    )
    val topics = Set("kafka_hzgc")
    val result = (1 to 2).map(_ =>KafkaUtils.
      createDirectStream[String, Student, StringDecoder, StudentDecoder](ssc, kafkaParams, topics))
    println(result+"**************************")
    val unionDS = ssc.union(result)
    unionDS.print()
//    val dStream = KafkaUtils.
//      createDirectStream[String, Student, StringDecoder, StudentDecoder](ssc, kafkaParams, topics)
//
//    dStream.map(elem => (elem._1, elem._2)).foreachRDD(rdd =>{
//      rdd.foreachPartition(par =>{
//        par.foreach(elem =>{
//          val key = elem._1
//          val id = elem._2.getId
//          val name = elem._2.getName
//          println("key:"+key+"  id:"+id+"  name:"+name)
//        })
//      })
//    })
    ssc.start()
    ssc.awaitTermination()


  }

}
