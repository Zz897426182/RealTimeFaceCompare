package com.hzgc.cluster.alarm

import java.text.SimpleDateFormat
import java.util.Date

import com.google.gson.Gson
import com.hzgc.service.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.service.staticrepo.ObjectInfoInnerHandlerImpl
import com.hzgc.cluster.message.OffLineAlarmMessage
import com.hzgc.cluster.util.PropertiesUtils
import com.hzgc.ftpserver.producer.RocketMQProducer
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters

/**
  * 人脸识别离线告警任务（刘善彬）
  *
  */

object FaceOffLineAlarmJob {
  def main(args: Array[String]): Unit = {
    val offLineAlarmMessage = new OffLineAlarmMessage()
    val properties = PropertiesUtils.getProperties
    val appName = properties.getProperty("job.offLine.appName")
    val conf = new SparkConf()
      .setAppName(appName)
    val sc = new SparkContext(conf)
    val deviceUtilImpl = new DeviceUtilImpl()
    val offLineAlarmRule = deviceUtilImpl.getThreshold
    val separator = "ZHONGXIAN"
    if (offLineAlarmRule != null && !offLineAlarmRule.isEmpty) {
      println("Start offline alarm task data processing ...")
      val objTypeList = PropertiesUtils.getOffLineArarmObjType(offLineAlarmRule)
      val returnResult = ObjectInfoInnerHandlerImpl.getInstance().searchByPkeysUpdateTime(objTypeList)
      if (returnResult != null && !returnResult.isEmpty) {
        val totalData = sc.parallelize(JavaConverters.asScalaBufferConverter(returnResult).asScala)
        val splitResult = totalData.map(totailDataElem => (totailDataElem.split(separator)(0), totailDataElem.split(separator)(1), totailDataElem.split(separator)(2)))
        val getDays = splitResult.map(splitResultElem => {
          val objRole = offLineAlarmRule.get(splitResultElem._2)
          if (objRole == null && objRole.isEmpty) {
            (splitResultElem._1, splitResultElem._2, splitResultElem._3, null)
          } else {
            val days = PropertiesUtils.getSimilarity(objRole)
            (splitResultElem._1, splitResultElem._2, splitResultElem._3, days)
          }
        }).filter(_._4 != null)
        val filterResult = getDays.filter(getFilter => getFilter._3 != null && getFilter._3.length != 0).
          map(getDaysElem => (getDaysElem._1, getDaysElem._2, getDaysElem._3, PropertiesUtils.timeTransition(getDaysElem._3), getDaysElem._4)).
          filter(ff => ff._4 != null && ff._4.length != 0).
          filter(filter => filter._4 > filter._5.toString)
        //将离线告警信息推送到MQ()
        filterResult.foreach(filterResultElem => {
          val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          val dateStr = df.format(new Date())
          val rocketMQProducer = RocketMQProducer.getInstance()
          val offLineAlarmMessage = new OffLineAlarmMessage()
          val gson = new Gson()
          offLineAlarmMessage.setAlarmType(DeviceTable.OFFLINE.toString)
          offLineAlarmMessage.setStaticID(filterResultElem._1)
          offLineAlarmMessage.setUpdateTime(filterResultElem._3)
          offLineAlarmMessage.setAlarmTime(dateStr)
          val alarmStr = gson.toJson(offLineAlarmMessage)
          //离线告警信息推送的时候，平台id为对象类型字符串的前4个字节。
          val platID = filterResultElem._2.substring(0, 4)
          rocketMQProducer.send(platID, "alarm_" + DeviceTable.OFFLINE.toString, filterResultElem._1 + dateStr, alarmStr.getBytes(), null);
        })
      } else {
        println("No data was received from the static repository,the task is not running！")
      }
    } else {
      println("No object type alarm dispatched offline,the task is not running")
    }
    sc.stop()
  }


}
