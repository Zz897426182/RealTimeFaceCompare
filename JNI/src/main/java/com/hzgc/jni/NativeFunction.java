package com.hzgc.jni;

import com.hzgc.dubbo.feature.FaceAttribute;

import java.io.Serializable;

public class NativeFunction implements Serializable {
    /**
     * @param faceAttribute 人脸属性
     * @param data
     * @param width         图片宽度
     * @param height        图片高度
     * @return int 0:success 1：failure
     */
    public static native int feature_extract(FaceAttribute faceAttribute, int[] data, int width, int height);

    public static native void init();

    public static native void destory();

    public static native float compare(float[] currentFeature, float[] historyFeature);

    static {
        System.loadLibrary("FaceLib");
    }
}
