package com.hzgc.dubbo.attribute;


import java.io.Serializable;

/**
 * 头发颜色：0->无；1->金色；2->黑色；3->棕色；4->灰白
 */
public enum HairColor implements Serializable {
    None(0), Blond(1), Black(2), Brown(3), Gray(4);

    private int value;

    /**
     * 与其他属性的拼接运算，默认是OR运算
     */
    private Logistic logistic = Logistic.OR;

    private HairColor(int value) {
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

    public static HairColor get(int haircolorvalue) {
        for (HairColor hairColor : HairColor.values()) {
            if (haircolorvalue == hairColor.getValue()) {
                return hairColor;
            }
        }
        return HairColor.None;
    }

    /**
     * 获取属性描述
     *
     * @param hairColor 属性对象
     * @return 属性描述信息
     */
    public static String getDesc(HairColor hairColor) {
        if (hairColor == HairColor.None) {
            return "无";
        } else if (hairColor == HairColor.Blond) {
            return "金色";
        } else if (hairColor == HairColor.Black) {
            return "黑色";
        } else if (hairColor == HairColor.Brown) {
            return "棕色";
        } else if (hairColor == HairColor.Gray) {
            return "灰白";
        }
        return null;
    }
}
