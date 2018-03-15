package com.hzgc.jni;

import com.hzgc.dubbo.feature.FaceAttribute;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;

public class FaceFunction {
    private static final String SPLIT = ":";

    public static byte[] getPictureBytes(String picPath) {
        File imageFile;
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {
            imageFile = new File(picPath);
            if (imageFile.exists()) {
                baos = new ByteArrayOutputStream();
                fis = new FileInputStream(imageFile);
                int len;
                while ((len = fis.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //assert baos != null;
        if (baos == null) {
            return null;
        }
        return baos.toByteArray();
    }

    /**
     * 特征提取方法 （内）（赵喆）
     *
     * @param imageData 将图片转为字节数组传入
     * @return 输出float[]形式的特征值
     */
    public static FaceAttribute featureExtract(byte[] imageData) {
        BufferedImage faceImage;
        try {
            if (null != imageData) {
                FaceAttribute faceAttribute = new FaceAttribute();
                int successOrfailue;
                faceImage = ImageIO.read(new ByteArrayInputStream(imageData));
                int height = faceImage.getHeight();
                int width = faceImage.getWidth();

                // 判断图片清晰度，0为清晰，1为不清晰
                if (height > 80 && width > 80) {
                    faceAttribute.setSharpness(0);
                } else {
                    faceAttribute.setSharpness(1);
                }

                int[] rgbArray = new int[height * width * 3];
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int pixel = faceImage.getRGB(w, h);//RGB颜色模型
                        rgbArray[h * width * 3 + w * 3] = (pixel & 0xff0000) >> 16;//0xff0000 红色
                        rgbArray[h * width * 3 + w * 3 + 1] = (pixel & 0xff00) >> 8;
                        rgbArray[h * width * 3 + w * 3 + 2] = (pixel & 0xff);
                    }
                }
                successOrfailue = NativeFunction.feature_extract(faceAttribute, rgbArray, width, height);
                if (successOrfailue == 0) {
                    return faceAttribute;
                } else {
                    return new FaceAttribute();
                }
            } else {
                throw new NullPointerException("The data of picture is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FaceAttribute();
    }

    /**
     * 特征提取方法 （内）（赵喆）
     *
     * @param imagePath 传入图片的绝对路径
     * @return 返回float[]形式的特征值
     */
    public static FaceAttribute featureExtract(String imagePath) {
        File imageFile;
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {
            imageFile = new File(imagePath);
            if (imageFile.exists()) {
            }
            baos = new ByteArrayOutputStream();
            fis = new FileInputStream(imageFile);
            int len;
            while ((len = fis.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            bais = new ByteArrayInputStream(baos.toByteArray());
            BufferedImage image = ImageIO.read(bais);
            if (image != null) {
                FaceAttribute faceAttribute = new FaceAttribute();
                int successOrfailue;
                int height = image.getHeight();
                int width = image.getWidth();
                int[] rgbArray = new int[height * width * 3];
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int pixel = image.getRGB(w, h);// 下面三行代码将一个数字转换为RGB数字
                        rgbArray[h * width * 3 + w * 3] = (pixel & 0xff0000) >> 16;
                        rgbArray[h * width * 3 + w * 3 + 1] = (pixel & 0xff00) >> 8;
                        rgbArray[h * width * 3 + w * 3 + 2] = (pixel & 0xff);
                    }
                }
                successOrfailue = NativeFunction.feature_extract(faceAttribute, rgbArray, width, height);
                if (successOrfailue == 0) {
                    return faceAttribute;
                } else {
                    return new FaceAttribute();
                }
            } else {
                throw new FileNotFoundException(imageFile.getName() + " is not exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bais) {
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new FaceAttribute();
    }

    /**
     * 将特征值（float[]）转换为字符串（String）（内）（赵喆）
     *
     * @param feature 传入float[]类型的特征值
     * @return 输出指定编码为UTF-8的String
     */
    public static String floatArray2string(float[] feature) {
        if (feature != null && feature.length == 512) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < feature.length; i++) {
                if (i == 511) {
                    sb.append(feature[i]);
                } else {
                    sb.append(feature[i]).append(":");
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 将特征值（String）转换为特征值（float[]）（内）（赵喆）
     *
     * @param feature 传入编码为UTF-8的String
     * @return 返回float[]类型的特征值
     */
    public static float[] string2floatArray(String feature) {
        if (feature != null && feature.length() > 0) {
            float[] featureFloat = new float[512];
            String[] strArr = feature.split(SPLIT);
            for (int i = 0; i < strArr.length; i++) {
                featureFloat[i] = Float.valueOf(strArr[i]);
            }
            return featureFloat;
        }
        return new float[0];
    }

    /**
     * 相似度比较，范围[0-1]
     *
     * @param currentFeatureStr 需要比对的特征值
     * @param historyFeatureStr 库中的特征值
     * @return 相似度
     */
    public static float featureCompare(String currentFeatureStr, String historyFeatureStr) {
        float[] currentFeature = FaceFunction.string2floatArray(currentFeatureStr);
        float[] historyFeature = FaceFunction.string2floatArray(historyFeatureStr);
        return featureCompare(currentFeature, historyFeature);
    }

    /**
     * 将byte[]型特征转化为float[]
     *
     * @param fea byte[]型特征
     * @return float[]
     */
    public static float[] byteArr2floatArr(byte[] fea) {
        if (null != fea && fea.length > 0) {
            return string2floatArray(new String(fea));
        }
        return new float[0];
    }

    public static float featureCompare(float[] currentFeature, float[] historyFeature) {
        double similarityDegree = 0;
        double currentFeatureMultiple = 0;
        double historyFeatureMultiple = 0;
        for (int i = 0; i < currentFeature.length; i++) {
            similarityDegree = similarityDegree + currentFeature[i] * historyFeature[i];
            currentFeatureMultiple = currentFeatureMultiple + Math.pow(currentFeature[i], 2);//pow 返回currentFeature[i] 平方
            historyFeatureMultiple = historyFeatureMultiple + Math.pow(historyFeature[i], 2);
        }

        double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);//sqrt 平方根 余弦相似度
        double actualValue = new BigDecimal((0.5 + (tempSim / 2)) * 100).//余弦相似度表示为cosineSIM=0.5cosθ+0.5
                setScale(2, BigDecimal.ROUND_HALF_UP).//ROUND_HALF_UP=4 保留两位小数四舍五入
                doubleValue();
        if (actualValue >= 100) {
            return 100;
        }
        return (float) actualValue;
    }
}
