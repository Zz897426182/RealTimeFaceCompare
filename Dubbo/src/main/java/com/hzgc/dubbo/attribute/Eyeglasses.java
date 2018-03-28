package com.hzgc.dubbo.attribute;

import java.io.Serializable;

/**
 * 是否戴眼镜：0->无；1->戴眼镜；2->没有戴眼镜；
 */
public enum Eyeglasses implements Serializable {
    None(0), Eyeglasses_y(1), Eyeglasses_n(2);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private Eyeglasses(int value) {
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

    public static Eyeglasses get(int value) {
        for (Eyeglasses eyeglasses : Eyeglasses.values()) {
            if (value == eyeglasses.getValue()) {
                return eyeglasses;
            }
        }
        return Eyeglasses.None;
    }

    /**
     * 获取属性描述
     *
     * @param eyeglasses 属性对象
     * @return 属性描述信息
     */
    public static String getDesc(Eyeglasses eyeglasses) {
        if (eyeglasses == Eyeglasses.None) {
            return "无";
        } else if (eyeglasses == Eyeglasses.Eyeglasses_y) {
            return "戴眼镜";
        } else if (eyeglasses == Eyeglasses.Eyeglasses_n) {
            return "没有戴眼镜";
        }
        return null;
    }
}
