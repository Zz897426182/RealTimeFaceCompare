package com.hzgc.dubbo.dynamic;

import java.util.Map;

/**
 * 动态图片定义
 */
public class CapturedPicture {
    /**
     * 图片 id 用于获取图片
     */
    private String id;
    /**
     * 捕获照片的设备 id
     */
    private String ipcId;
    /**
     * 图片的描述信息
     */
    private String description;
    /**
     * 图片的相似度
     */
    private int similarity;
    /**
     * 图片的附加信息，扩展预留
     */
    private Map<String, Object> extend;
    /**
     * 图片数据
     */
    private byte[] image;
}
