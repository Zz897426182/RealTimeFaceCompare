package com.hzgc.streaming.job

import java.text.SimpleDateFormat
import java.util.Date
import com.google.gson.Gson
import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.dynamicrepo.{CapturePictureSearchServiceImpl, GetPicture}
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.jni.FaceFunction
import com.hzgc.rocketmq.util.RocketMQProducer
import com.hzgc.streaming.message.AddAlarmMessage
import com.hzgc.streaming.util.StreamingUtils
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
    val gp = new GetPicture()
    val properties = StreamingUtils.getProperties
    val appName = properties.getProperty("job.addAlarm.appName")
    val master = properties.getProperty("job.addAlarm.master")
    val timeInterval = Durations.seconds(properties.getProperty("job.addAlarm.timeInterval").toLong)
    val conf = new SparkConf().setAppName(appName).setMaster(master)
    val ssc = new StreamingContext(conf, timeInterval)

    val kafkaGroupId = properties.getProperty("kafka.FaceAddAlarmJob.group.id")
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
          val addWarnRule = alarmRule.get(DeviceTable.ADDED)
          if (addWarnRule != null && !addWarnRule.isEmpty) {
            totalList.foreach(record => {
              if (addWarnRule.containsKey(record(1))) {
                val threshold = FaceFunction.featureCompare(record(2), message._2)
                if (threshold > addWarnRule.get(record(1))) {
                  filterResult += Json(record(0), record(1), threshold)
                }
              }
            })
            val finalResult = filterResult.sortWith(_.sim > _.sim).take(3)
            (message._1, ipcID, platID, finalResult)
          } else {
            println("This device [" + ipcID + "] does not bind to added the alarm rule, which is not calculated by default")
            (message._1, ipcID, null, filterResult)
          }
        } else {
          println("This device [" + ipcID + "] does not bind the alarm rules and is not calculated by default")
          (message._1, ipcID, null, filterResult)
        }
      } else {
        println("This device [" + ipcID + "] does not have a binding platform ID, which is not calculated by default")
        (message._1, ipcID, null, filterResult)
      }
    }).filter(jsonResultFilter => jsonResultFilter._3 != null)

    jsonResult.foreachRDD(resultRDD => {
      resultRDD.foreachPartition(parRDD => {
        val rocketMQProducer = RocketMQProducer.getInstance()
        val gson = new Gson()
        val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        parRDD.foreach(result => {
          //识别集合为null，对该条数据进行新增告警。
          if (result._4 == null || result._4.isEmpty) {
            var flag = gp.getCapture(result._1)
            if (flag == null) {
              Thread.sleep(5000)
            }
            flag = gp.getCapture(result._1)
            if (flag != null) {
              val dateStr = df.format(new Date())
              val addAlarmMessage = new AddAlarmMessage()
              addAlarmMessage.setAlarmTime(dateStr)
              addAlarmMessage.setAlarmType(DeviceTable.ADDED.toString)
              addAlarmMessage.setDynamicID(result._1)
              addAlarmMessage.setDynamicDeviceID(result._2)
              rocketMQProducer.send(result._3,
                "alarm_" + DeviceTable.ADDED.toString,
                result._1,
                gson.toJson(addAlarmMessage).getBytes(),
                null)
            }else{
              println("*********  Hbase no photo [DynamicID = "+result._1+"]  *********")
            }
          }
        })
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }

}
