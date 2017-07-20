package com.hzgc.dubbo.dynamicrepo;


public interface DynamicPhotoService {
    /**
     * 获取动态图片信息（外）（刘善彬）
     * @param DynamicPhotoID 动态图片ID
     * @return 动态图片的具体信息（大图、小图等等）
     */
    public PersonPhoto getDynamicPhotoInfo(String DynamicPhotoID);






}
