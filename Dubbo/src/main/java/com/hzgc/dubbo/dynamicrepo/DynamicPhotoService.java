package com.hzgc.dubbo.dynamicrepo;


import java.util.List;
import java.util.Map;

public interface DynamicPhotoService {

    /**
     * 将rowKey、特征值插入人脸/车辆特征库 （内）（刘思阳）
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    public boolean insertPictureFeature(PictureType type, String rowKey, float[] feature);

    /**
     *
     * @param type
     * @param rowKey
     * @param feature
     * @param image
     * @return
     */
    public boolean upPictureInsert(PictureType type, String rowKey, float[] feature, byte[] image);
    /**
     *
     * @param imageId
     * @param type
     * @return
     */
    public byte[] getFeature(String imageId, PictureType type);

    /**
     *
     * @param searchId
     * @param queryImageId
     * @param resMap
     * @return
     */
    public boolean insertSearchRes(String searchId, String queryImageId, Map<String, Float> resMap);
    /**
     *
     * @param searchID
     * @return
     */
    public Map<String, Float> getSearchRes(String searchID);
}
