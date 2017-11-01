package com.hzgc.streaming.job

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.dynamicrepo.{CapturePictureSearchServiceImpl, GetPicture}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.message.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util.StreamingUtils
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

import scala.collection.JavaConverters
import scala.collection.mutable.ArrayBuffer

/**
  * 人脸识别告警实时计算任务
  */
object FaceRecognizeAlarmJob {

  case class Json(staticID: String,
                  staticObjectType: String,
                  sim: Float)

  def main(args: Array[String]): Unit = {
    val deviceUtilI = new DeviceUtilImpl()
    val gp = new GetPicture()
    val properties = StreamingUtils.getProperties
    val appName = properties.getProperty("job.recognizeAlarm.appName")
    val master = properties.getProperty("job.recognizeAlarm.master")
    val itemNum = properties.getProperty("job.recognizeAlarm.items.num").toInt
    val timeInterval = Durations.seconds(properties.getProperty("job.recognizeAlarm.timeInterval").toLong)
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, timeInterval)

    val kafkaGroupId = properties.getProperty("kafka.FaceRecognizeAlarmJob.group.id")
    val topics = Set(properties.getProperty("kafka.topic.name"))
    val brokers = properties.getProperty("kafka.metadata.broker.list")
    val kafkaParams = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId
    )
    val kafkaDynamicPhoto = KafkaUtils.
      createDirectStream[String, String, StringDecoder, FeatureDecoder](ssc, kafkaParams, topics)
    val jsonResult = kafkaDynamicPhoto.map(message => {
      val totalList = JavaConverters.asScalaBufferConverter(ObjectInfoInnerHandlerImpl.getInstance().getTotalList).asScala
      val ipcID = FtpUtil.getRowKeyMessage(message._1).get("ipcID")
      val platID = deviceUtilI.getplatfromID(ipcID)
      val alarmRule = deviceUtilI.isWarnTypeBinding(ipcID)
      val filterResult = new ArrayBuffer[Json]()
      if (platID != null && platID.length > 0) {
        if (alarmRule != null && !alarmRule.isEmpty) {
          val recognizeWarnRule = alarmRule.get(DeviceTable.IDENTIFY)
          if (recognizeWarnRule != null && !recognizeWarnRule.isEmpty) {
            totalList.foreach(record => {
              if (recognizeWarnRule.containsKey(record(1))) {
                val threshold = FaceFunction.featureCompare(record(2), message._2)
                if (threshold > recognizeWarnRule.get(record(1))) {
                  filterResult += Json(record(0), record(1), threshold)
                }
              }
            })
          } else {
            println("This device [" + ipcID + "] does not bind to recognize the alarm rule, which is not calculated by default")
          }
        } else {
          println("This device [" + ipcID + "] does not bind the alarm rules and is not calculated by default")
        }
      } else {
        println("This device [" + ipcID + "] does not have a binding platform ID, which is not calculated by default")
      }
      val finalResult = filterResult.sortWith(_.sim > _.sim).take(itemNum)
      val updateTimeList = new util.ArrayList[String]()
      if (alarmRule != null && platID != null) {
        val offLineWarnRule = alarmRule.get(DeviceTable.OFFLINE)
        if (offLineWarnRule != null && !offLineWarnRule.isEmpty) {
          finalResult.foreach(record => {
            if (offLineWarnRule.containsKey(record.staticObjectType)) {
              updateTimeList.add(record.staticID)
            }
          })
        }
      }
      ObjectInfoInnerHandlerImpl.getInstance().updateObjectInfoTime(updateTimeList)
      (message._1, ipcID, platID, finalResult)
    }).filter(record => record._4.nonEmpty)

    jsonResult.foreachRDD(resultRDD => {
      resultRDD.foreachPartition(parRDD => {
        val rocketMQProducer = RocketMQProducer.getInstance()
        val gson = new Gson()
        val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        parRDD.foreach(result => {
          var flag = gp.getCapture(result._1)
          if (flag == null) {
            Thread.sleep(5000)
          }
          flag = gp.getCapture(result._1)
          if (flag != null) {
            val recognizeAlarmMessage = new RecognizeAlarmMessage()
            val items = new ArrayBuffer[Item]()
            val dateStr = df.format(new Date())
            recognizeAlarmMessage.setAlarmType(DeviceTable.IDENTIFY.toString)
            recognizeAlarmMessage.setDynamicDeviceID(result._2)
            recognizeAlarmMessage.setDynamicID(result._1)
            recognizeAlarmMessage.setAlarmTime(dateStr)
            result._4.foreach(record => {
              val item = new Item()
              item.setSimilarity(record.sim.toString)
              item.setStaticID(record.staticID)
              items += item
            })
            recognizeAlarmMessage.setItems(items.toArray)
            rocketMQProducer.send(result._3,
              "alarm_" + DeviceTable.IDENTIFY.toString,
              result._1,
              gson.toJson(recognizeAlarmMessage).getBytes(),
              null)

          }else{
            println("*********  Hbase no photo [DynamicID = "+result._1+"]  *********")
          }
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}