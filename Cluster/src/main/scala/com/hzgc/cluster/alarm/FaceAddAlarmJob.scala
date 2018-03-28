package com.hzgc.cluster.alarm

import java.text.SimpleDateFormat
import java.util.Date

import com.google.gson.Gson
import com.hzgc.service.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.service.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.cluster.message.AddAlarmMessage
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.ftpserver.producer.{FaceObject, FaceObjectDecoder, RocketMQProducer}
import com.hzgc.ftpserver.util.FtpUtils
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer

/**
  * 人脸新增告警实时处理任务（刘善彬）
  */
object FaceAddAlarmJob {

  case class Json(staticID: String,
                  staticObjectType: String,
                  sim: Float)

  def main(args: Array[String]): Unit = {
    val deviceUtilI = new DeviceUtilImpl()
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.addAlarm.appName")
    val timeInterval = Durations.seconds(properties.getProperty("job.addAlarm.timeInterval").toLong)
    val conf = new SparkConf()
      .setAppName(appName)
    val ssc = new StreamingContext(conf, timeInterval)
    val kafkaGroupId = properties.getProperty("kafka.FaceAddAlarmJob.group.id")
    val topics = Set(properties.getProperty("kafka.topic.name"))
    val brokers = properties.getProperty("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    val kafkaDynamicPhoto = KafkaUtils.
      createDirectStream[String, FaceObject, StringDecoder, FaceObjectDecoder](ssc, kafkaParams, topics)
    val jsonResult = kafkaDynamicPhoto.
      filter(obj => obj._2.getAttribute.getFeature != null).
      filter(f => f._2.getAttribute.getFeature.length == 512).
      map(message => {
        val totalList = JavaConverters.
          asScalaBufferConverter(ObjectInfoInnerHandlerImpl.getInstance().getTotalList).asScala
        val faceObj = message._2
        val ipcID = faceObj.getIpcId
        val platID = deviceUtilI.getplatfromID(ipcID)
        val alarmRule = deviceUtilI.isWarnTypeBinding(ipcID)
        val filterResult = new ArrayBuffer[Json]()
        if (platID != null && platID.length > 0) {
          if (alarmRule != null && !alarmRule.isEmpty) {
            val addWarnRule = alarmRule.get(DeviceTable.ADDED)
            if (addWarnRule != null && !addWarnRule.isEmpty) {
              totalList.foreach(record => {
                if (addWarnRule.containsKey(record(1))) {
                  val threshold = FaceFunction.featureCompare(record(2).asInstanceOf[Array[Float]], faceObj.getAttribute.getFeature)
                  if (threshold > addWarnRule.get(record(1))) {
                    filterResult += Json(record(0).asInstanceOf[String], record(1).asInstanceOf[String], threshold)
                  }
                }
              })
              val finalResult = filterResult.sortWith(_.sim > _.sim).take(3)
              (message._1, ipcID, platID, finalResult)
            } else {
              println("Device [" + ipcID + "] does not bind added alarm rule,current time [" + df.format(new Date()) + "]")
              (message._1, ipcID, null, filterResult)
            }
          } else {
            println("Device [" + ipcID + "] does not bind alarm rule,current time [" + df.format(new Date()) + "]")
            (message._1, ipcID, null, filterResult)
          }
        } else {
          println("Device [" + ipcID + "] does not bind platform ID,current time [" + df.format(new Date()) + "]")
          (message._1, ipcID, null, filterResult)
        }
      }).filter(jsonResultFilter => jsonResultFilter._3 != null)

    jsonResult.foreachRDD(resultRDD => {
      resultRDD.foreachPartition(parRDD => {
        val rocketMQProducer = RocketMQProducer.getInstance()
        val gson = new Gson()
        parRDD.foreach(result => {
          //识别集合为null，对该条数据进行新增告警。
          if (result._4 == null || result._4.isEmpty) {
            val dateStr = df.format(new Date())
            val addAlarmMessage = new AddAlarmMessage()
            val ftpMess = FtpUtils.getFtpUrlMessage(result._1)
            addAlarmMessage.setAlarmTime(dateStr)
            addAlarmMessage.setAlarmType(DeviceTable.ADDED.toString)
            addAlarmMessage.setSmallPictureURL(ftpMess.get("filepath"))
            addAlarmMessage.setBigPictureURL(FtpUtils.getFtpUrlMessage(FtpUtils.surlToBurl(result._1)).get("filepath"))
            addAlarmMessage.setDynamicDeviceID(result._2)
            addAlarmMessage.setHostName(ftpMess.get("ip"))
            rocketMQProducer.send(result._3,
              "alarm_" + DeviceTable.ADDED.toString,
              result._1,
              gson.toJson(addAlarmMessage).getBytes(),
              null)
          }
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }

}
