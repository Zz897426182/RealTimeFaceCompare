package com.hzgc.cluster.clustering

import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.mutable

object Canopy {

  def main(args: Array[String]): Unit = {
    val master = args(0)
    val input = args(1)
    val slices = args(2).toInt
    val output = args(3)
    val t1 = args(4).toDouble
    val t2 = args(5).toDouble

    val lable_separator = "\001"
    val vector_separator = ","

    val log = LoggerFactory.getLogger("Canopy")
    val spark = SparkSession.builder().appName("Canopy").enableHiveSupport().master("local[*]").getOrCreate()
    val sc = spark.sparkContext
    try {
      val pairs = sc.textFile(input).map { line =>
        val pair = line.split(lable_separator)
        (pair(0), pair(1).split(vector_separator).map(_.toDouble))
      }
      val map_centers = new mutable.HashSet[(String, Array[Double])]
      val raw_center_pairs = pairs.map(v => (v._1, canopy_(v, map_centers, t2))).filter(a => a._2 != null).collect().toList

      val center_pairs = new mutable.HashSet[(String, Array[Double])]

      for (i <- raw_center_pairs.indices) {
        canopy_(raw_center_pairs(i)._2, center_pairs, t2)
      }
      sc.makeRDD(center_pairs.toList, 1).map { pair =>
        pair._1 + pair._2.mkString(",")
      }.saveAsTextFile(output)
    } catch {
      case e: Exception => log.info(e.getStackTrace.mkString("\n"))
    }
    sc.stop()
  }

  def measure(v1: Array[Double], v2: Array[Double]): Double = {
    var distance = 0.0
    val aa = if (v1.length < v2.length) v1.length else v2.length
    for (i <- 0 until aa) {
      distance += scala.math.pow(v1(i) - v2(i), 2)
    }
    distance
  }

  def canopy_(p0: (String, Array[Double]), pair: mutable.HashSet[(String, Array[Double])], t2: Double): (String, Array[Double]) = {
    if (!pair.exists(p => measure(p._2, p0._2) < t2)) {
      pair += p0
      p0
    } else {
      null
    }
  }
}
