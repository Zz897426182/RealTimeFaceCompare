package com.hzgc.dubbo.dynamicrepo;

/**
 * 图片类型
 */
public enum PictureType {
    /**
     * 人脸图
     */
    PERSON(0),
    /**
     *车辆图
     */
    CAR(1);

    private int type;

    PictureType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 根据类型值获取 PictureType,默认为PERSON
     *
     * @param type 类型值
     * @return PictureType
     */
    public static PictureType get(int type) {
        for (PictureType pictureType : PictureType.values()) {
            if (type == pictureType.getType()) {
                return pictureType;
            }
        }
        return PictureType.PERSON;
    }

    @Override
    public String toString() {
        return "PictureType{" +
                "type=" + type +
                '}';
    }
}
