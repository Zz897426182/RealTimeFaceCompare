package com.hzgc.dubbo.attribute;

import java.io.Serializable;

/**
 * 是否带帽子：0->无；1->戴帽子；2->没有戴帽子；
 */
public enum Hat implements Serializable {
    None(0), Hat_y(1), Hat_n(2);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Hat(int value) {
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

    public static Hat get(int hatvalue) {
        for (Hat hat : Hat.values()) {
            if (hatvalue == hat.getValue()) {
                return hat;
            }
        }
        return Hat.None;
    }

    /**
     * 获取属性描述
     *
     * @param hat 属性对象
     * @return 属性描述信息
     */
    public static String getDesc(Hat hat) {
        if (hat == Hat.None) {
            return "无";
        } else if (hat == Hat.Hat_y) {
            return "戴帽子";
        } else if (hat == Hat.Hat_n) {
            return "没有戴帽子";
        }
        return null;
    }
}
