package com.hzgc.dubbo.feature;

public interface FaceExtract {
    /**
     * 特征提取
     *
     * @param imageBytes 图片的字节数组
     * @return float[] 特征值:长度为2048的float[]数组
     */
    FaceAttribute featureExtract(byte[] imageBytes);
}
