package com.hzgc.ftpserver.producer;


import com.hzgc.ftpserver.util.BeanUtils;
import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

public class FaceObjectDecoder implements Decoder<FaceObject> {
    public FaceObjectDecoder(VerifiableProperties verifiableProperties) {
    }
    @Override
    public FaceObject fromBytes(byte[] bytes) {
        return BeanUtils.bytesToObject(bytes);
    }
}
