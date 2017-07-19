package com.hzgc.dubbo.dynamic;

/**
 * 图片类型
 */
public enum PictureType {
    /**
     * 默认类型，表示需要进行分析图片类型
     */
    DEFAULT(0),
    /**
     * 人脸图
     */
    FACE(1),
    /**
     * 车辆图
     */
    CAR(100);

    private int type;

    PictureType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * 根据类型值获取 PictureType
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
        return PictureType.DEFAULT;
    }
}
