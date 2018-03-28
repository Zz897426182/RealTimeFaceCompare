package com.hzgc.dubbo.dynamicrepo;

import java.util.List;
import java.util.Map;

public interface DynamicPhotoService {

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
    boolean upPictureInsert(SearchType type, String rowKey, float[] feature, byte[] image);

    /**
     * 将查询ID、查询相关信息插入查询结果库 （内）（刘思阳）
     * 表名：searchRes
     *
     * @param searchId            查询ID（rowKey）
     * @param capturedPictureList 查询信息（返回图片ID、相识度）
     * @return boolean 是否插入成功
     */
    boolean insertSearchRes(String searchId, List<CapturedPicture> capturedPictureList, String insertType);

}
