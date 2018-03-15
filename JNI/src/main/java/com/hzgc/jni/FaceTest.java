package com.hzgc.jni;

import com.hzgc.dubbo.feature.FaceAttribute;

public class FaceTest {
    private FaceTest() {
        NativeFunction.init();

    }
    public static void main(String[] args) {
        if (args.length == 1) {
            FaceTest faceTest = new FaceTest();
//            faceTest.featureExtract(args[0]);
            faceTest.featureCompare(args[0]);
        } else {
            System.out.println("参数个数为1,传入一张图片的绝对路径测试特征提取和特征比对");
        }
    }

    private void featureExtract(String picture) {
        try {
            FaceFunction.featureExtract(picture);
            System.out.println("Verify through...");
        } catch (Exception e) {
            System.out.println("Validation does not pass...");
        }
    }

    private void featureCompare(String picture) {
        System.out.println("+++++++++++++++++++");
        FaceAttribute attribute = FaceFunction.featureExtract(picture);
        long start = System.currentTimeMillis();
        float[] pp = new float[204800000];
//        for (int i =0; i < 400000; i++) {
//            FaceFunction.featureCompare(attribute.getFeature(), attribute.getFeature());
            NativeFunction.compare(pp, pp);
//        }
        System.out.println("400000 feature compare total time is " + (System.currentTimeMillis() - start));
        System.out.println("Similarity degree is "
                + FaceFunction.featureCompare(attribute.getFeature(), attribute.getFeature()));
    }
}