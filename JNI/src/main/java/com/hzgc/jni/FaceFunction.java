package com.hzgc.jni;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class FaceFunction {

    /**
     * 特征提取方法 （内）（赵喆）
     *
     * @param imageData 将图片转为字节数组传入
     * @return 输出float[]形式的特征值
     */
    public static float[] featureExtract(byte[] imageData) {
        float[] feature = null;
        BufferedImage faceImage;
        try {
            if (null != imageData) {
                faceImage = ImageIO.read(new ByteArrayInputStream(imageData));
                int height = faceImage.getHeight();
                int width = faceImage.getWidth();
                int[] rgbArray = new int[height * width * 3];
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int pixel = faceImage.getRGB(w, h);
                        rgbArray[h * width * 3 + w * 3] = (pixel & 0xff0000) >> 16;
                        rgbArray[h * width * 3 + w * 3 + 1] = (pixel & 0xff00) >> 8;
                        rgbArray[h * width * 3 + w * 3 + 2] = (pixel & 0xff);
                    }
                }
                feature = NativeFunction.feature_extract(rgbArray, width, height);
                return feature;
            } else {
                throw new NullPointerException("The data of picture is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return feature;
    }

    /**
     * 特征提取方法 （内）（赵喆）
     *
     * @param imagePath 传入图片的绝对路径
     * @return 返回float[]形式的特征值
     */
    public static float[] featureExtract(String imagePath) {
        float[] feature = null;
        File imageFile;
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;
        byte[] buffer = new byte[1024];
        try {
            imageFile = new File(imagePath);
            if (imageFile.exists()) {
                baos = new ByteArrayOutputStream();
                fis = new FileInputStream(imageFile);
                int len = 0;
                while ((len = fis.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                bais = new ByteArrayInputStream(baos.toByteArray());
                BufferedImage image = ImageIO.read(bais);
                int height = image.getHeight();
                int width = image.getWidth();
                int[] rgbArray = new int[height * width * 3];
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int pixel = image.getRGB(w, h);
                        rgbArray[h * width * 3 + w * 3] = (pixel & 0xff0000) >> 16;
                        rgbArray[h * width * 3 + w * 3 + 1] = (pixel & 0xff00) >> 8;
                        rgbArray[h * width * 3 + w * 3 + 2] = (pixel & 0xff);
                    }
                }
                feature = NativeFunction.feature_extract(rgbArray, width, height);
                return feature;
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
        return feature;
    }

    /**
     * 将特征值（float[]）转换为字符串（String）（内）（赵喆）
     *
     * @param feature 传入float[]类型的特征值
     * @return 输出指定编码为ISO-8859-1的String
     * @throws Exception
     */
    public static String floatArray2string(float[] feature) {
        if (null != feature && feature.length > 0) {
            try {
                byte[] byteFeature = new byte[feature.length * 4];
                int temp = 0;
                for (float f : feature) {
                    byte[] tempbyte = float2byte(f);
                    for (int i = 0; i < tempbyte.length; i++) {
                        byteFeature[temp + i] = tempbyte[i];
                    }
                    temp = temp + 4;
                }
                return new String(byteFeature, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 将特征值（String）转换为特征值（float[]）（内）（赵喆）
     *
     * @param feature 传入编码为ISO-8859-1的String
     * @return 返回float[]类型的特征值
     * @throws Exception
     */
    public static float[] string2floatArray(String feature) {
        float[] floatFeature;
        if (null != feature && feature.length() > 0) {
            try {
                byte[] byteFeature = feature.getBytes("ISO-8859-1");
                floatFeature = new float[byteFeature.length / 4];
                byte[] buffer = new byte[4];
                int countByte = 0;
                int countFloat = 0;
                while (countByte < byteFeature.length && countFloat < floatFeature.length) {
                    buffer[0] = byteFeature[countByte];
                    buffer[1] = byteFeature[countByte + 1];
                    buffer[2] = byteFeature[countByte + 2];
                    buffer[3] = byteFeature[countByte + 3];
                    if (countByte % 4 == 0) {
                        floatFeature[countFloat] = byte2float(buffer, 0);
                    }
                    countByte = countByte + 4;
                    countFloat++;
                }
                return floatFeature;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 内部方法：将字节数组转为float
     *
     * @param featureByte 传入字节数组
     * @param index       从指定位置开始读取4个字节
     * @return 返回4个字节组成的float数组
     */
    private static float byte2float(byte[] featureByte, int index) {
        int l;
        l = featureByte[index + 0];
        l &= 0xff;
        l |= ((long) featureByte[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) featureByte[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) featureByte[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    /**
     * 内部方法：将float转为4个长度的字节数组
     *
     * @param featureFloat 传入单一float
     * @return 返回4ge长度的字节数组
     */
    private static byte[] float2byte(float featureFloat) {
        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(featureFloat);

        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            buffer[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = buffer.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(buffer, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }
        return dest;
    }

    /**
     * 将byte[]型特征转化为float[]
     *
     * @param fea
     * @return float[]
     * @throws Exception
     */
    public static float[] byteArr2floatArr(byte[] fea) throws Exception {
        float[] newFea = FaceFunction.string2floatArray(new String(fea, "ISO-8859-1"));
        return newFea;
    }
}
