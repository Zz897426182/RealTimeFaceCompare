package com.hzgc.dubbo.dynamicrepo;


public interface DynamicPhotoService {

    /**
     * 将rowKey、特征值插入人脸/车辆特征库 （内）（刘思阳）
     *
     * @param type  图片类型（判断是车是人）
     * @param rowKey  图片id
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    public boolean insertePictureFeature(PictureType type, String rowKey, float[] feature);
}
