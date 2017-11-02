package com.hzgc.dubbo.dynamicrepo;

import java.util.List;
import java.util.Map;

public interface DynamicPhotoService {

    /**
     * 将rowKey、特征值插入人脸/车辆库 （内）（刘思阳）
     * 表名：person/car
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    boolean insertPictureFeature(PictureType type, String rowKey, float[] feature);

    /**
     * 根据小图rowKey获取小图特征值 （内）（刘思阳）
     * 表名：person/car
     *
     * @param imageId 小图rowKey
     * @param type    人/车
     * @return byte[] 小图特征值
     */
    byte[] getFeature(String imageId, PictureType type);

    /**
     * 批量获取特征值
     *
     * @param imageIdList 图片ID列表
     * @param type        查询类型
     * @return 特征值列表
     */
    List<float[]> getBatchFeature(List<String> imageIdList, PictureType type);

    /**
     * 批量多线程获取特征值
     *
     * @param imageIdList
     * @param type
     * @return
     */
    List<float[]> getMultiBatchFeature(List<String> imageIdList, PictureType type);

    /**
     * 将上传的图片、rowKey、特征值插入upFea特征库 （内）
     * 表名：upFea
     *
     * @param type    人/车
     * @param rowKey  上传图片ID（rowKey）
     * @param feature 特征值
     * @param image   图片
     * @return boolean 是否插入成功
     */
    boolean upPictureInsert(PictureType type, String rowKey, float[] feature, byte[] image);

    /**
     * 将查询ID、查询相关信息插入查询结果库 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchId            查询ID（rowKey）
     * @param capturedPictureList 查询信息（返回图片ID、相识度）
     * @return boolean 是否插入成功
     */
    boolean insertSearchRes(String searchId, List<CapturedPicture> capturedPictureList, String insertType);

    /**
     * 根据动态库查询ID获取查询结果 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchID 查询ID（rowKey）
     * @return 查询信息列表
     */
    Map<String, Float> getSearchRes(String searchID);

    /**
     * 根据id（rowKey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowKey）
     * @param type    图片类型，人/车
     * @return CapturedPicture
     */
    CapturedPicture getCaptureMessage(String imageId, int type);

    /**
     * 批量查询图片对象
     *
     * @param imageIdList 图片ID列表
     * @param type        搜索类型
     * @return List<CapturedPicture> 图片对象列表
     */
    List<CapturedPicture> getBatchCaptureMessage(List<String> imageIdList, int type);

    /**
     * 批量多线程查询图片对象
     *
     * @param imageIdList
     * @param type
     * @return
     */
    List<CapturedPicture> getMultiBatchCaptureMessage(List<String> imageIdList, int type);

    /**
     * 返回多张图片数据流
     *
     * @param capturedPictures
     * @param type
     * @return
     */
    List<CapturedPicture> getFullImageData(List<CapturedPicture> capturedPictures, int type);

    /**
     * 返回单张图片数据流
     *
     * @param capturedPicture
     * @return
     */
    CapturedPicture getImageData(CapturedPicture capturedPicture);

    /**
     * 返回多张图片数据流
     *
     * @param capturedPictures
     * @param type
     * @return
     */
    List<CapturedPicture> getImageData(List<CapturedPicture> capturedPictures, int type);
}
