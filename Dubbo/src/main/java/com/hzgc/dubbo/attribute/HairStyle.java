package com.hzgc.dubbo.attribute;

import java.io.Serializable;

/**
 * 头发类型：0->无；1->直发；2->卷发；3->光头；
 */
public enum HairStyle implements Serializable {
    None(0), Straight(1), Wavy(2), Bald(3);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private HairStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Logistic getLogistic() {
        return logistic;
    }

    public void setLogistic(Logistic logistic) {
        this.logistic = logistic;
    }

    public static HairStyle get(int hairstylevalue) {
        for (HairStyle hairStyle : HairStyle.values()) {
            if (hairstylevalue == hairStyle.getValue()) {
                return hairStyle;
            }
        }
        return HairStyle.None;
    }

    /**
     * 获取属性描述
     *
     * @param hairStyle 属性对象
     * @return 属性描述信息
     */
    public static String getDesc(HairStyle hairStyle) {
        if (hairStyle == HairStyle.None) {
            return "无";
        } else if (hairStyle == HairStyle.Straight) {
            return "直发";
        } else if (hairStyle == HairStyle.Wavy) {
            return "卷发";
        } else if (hairStyle == HairStyle.Bald) {
            return "光头";
        }
        return null;
    }
}
