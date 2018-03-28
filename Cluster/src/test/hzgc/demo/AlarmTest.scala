package demo

import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.ftpserver.producer.{FaceObject, FaceObjectDecoder}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

object AlarmTest {
  case class Json(staticID: String,
                  staticObjectType: String,
                  sim: Float)

  def main(args: Array[String]): Unit = {

    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.recognizeAlarm.appName")
    //    val master = properties.getProperty("job.recognizeAlarm.master")
    val itemNum = properties.getProperty("job.recognizeAlarm.items.num").toInt
    val timeInterval = Durations.seconds(properties.getProperty("job.recognizeAlarm.timeInterval").toLong)
    val conf = new SparkConf()
      .setAppName(appName)
      .setMaster("local[*]")
    val ssc = new StreamingContext(conf, timeInterval)

    val kafkaGroupId = properties.getProperty("kafka.FaceRecognizeAlarmJob.group.id")
    val topics = Set(properties.getProperty("kafka.topic.name"))
    val brokers = properties.getProperty("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val kafkaDynamicPhoto = KafkaUtils.
      createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](ssc, kafkaParams, topics)
    kafkaDynamicPhoto.map(elem => (elem._1,elem._2.getIpcId)).print()
    ssc.start()
    ssc.awaitTermination()
  }


}
