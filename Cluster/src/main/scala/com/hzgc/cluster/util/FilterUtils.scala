package com.hzgc.cluster.util


/**
  * 自定义过滤函数（刘善彬 To 内）
  */
object FilterUtils  {
  def similarityFilterFun(setSimilarity: String, similarity: Float): Boolean = {
    var bol = false
    if (setSimilarity.toFloat < similarity) {
      bol = true
    }
    bol
  }

  def rangeFilterFun(list: Array[String], elem: String): Boolean = {
    var flag = false
    list.foreach(a => {
      if (elem.equals(a)) {
        flag = true
      }
    })
    flag
  }

  def dayFilterFun(setSimilarity: String, similarity: String): Boolean = {
    var bol = false
    if (setSimilarity < similarity) {
      bol = true
    }
    bol
  }

}
