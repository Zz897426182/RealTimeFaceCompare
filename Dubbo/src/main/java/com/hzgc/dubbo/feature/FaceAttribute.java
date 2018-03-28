package com.hzgc.dubbo.feature;


import java.io.Serializable;
import java.util.Arrays;

public class FaceAttribute implements Serializable{
    /**
     * 特征值
     */
    private float[] feature;
    /**
     * 头发颜色
     */
    private int hairColor;
    /**
     * 头发类型
     */
    private int hairStyle;
    /**
     * 性别
     */
    private int gender;
    /**
     * 是否带帽子
     */
    private int hat;
    /**
     * 是否系领带
     */
    private int tie;
    /**
     * 胡子类型
     */
    private int huzi;
    /**
     * 是否戴眼镜
     */
    private int eyeglasses;

    public float[] getFeature() {
        return feature;
    }

    public void setFeature(float[] feature) {
        this.feature = feature;
    }

    public int getHairColor() {
        return hairColor;
    }

    public void setHairColor(int hairColor) {
        this.hairColor = hairColor;
    }

    public int getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(int hairStyle) {
        this.hairStyle = hairStyle;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getHat() {
        return hat;
    }

    public void setHat(int hat) {
        this.hat = hat;
    }

    public int getTie() {
        return tie;
    }

    public void setTie(int tie) {
        this.tie = tie;
    }

    public int getHuzi() {
        return huzi;
    }

    public void setHuzi(int huzi) {
        this.huzi = huzi;
    }

    public int getEyeglasses() {
        return eyeglasses;
    }

    public void setEyeglasses(int eyeglasses) {
        this.eyeglasses = eyeglasses;
    }

    @Override
    public String toString() {
        return "FaceAttribute{" +
                "feature=" + Arrays.toString(feature) +
                ", hairColor=" + hairColor +
                ", hairStyle=" + hairStyle +
                ", gender=" + gender +
                ", hat=" + hat +
                ", tie=" + tie +
                ", huzi=" + huzi +
                ", eyeglasses=" + eyeglasses +
                '}';
    }
}
