package com.hzgc.ftpserver.producer;

import com.hzgc.ftpserver.util.BeanUtils;

import java.util.Map;


public class FaceObjectEncoder implements org.apache.kafka.common.serialization.Serializer<FaceObject> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, FaceObject faceObject) {
        return BeanUtils.objectToBytes(faceObject);
    }

    @Override
    public void close() {

    }
}
