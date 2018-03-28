package com.hzgc.cluster.alarm

import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

class FeatureDecoder(props: VerifiableProperties = null) extends Decoder[String] {
  val encoding: String =
    if (props == null) {
      "ISO8859-1"
    } else {
      props.getString("serializer.encoding", "ISO8859-1")
    }
  override def fromBytes(bytes: Array[Byte]): String = {
    new String(bytes, encoding)
  }
}
