package com.hzgc.dubbo.dynamic;

import java.util.List;

/**
 * 搜索结果
 */
public class SearchResult {
    /**
     * 本次搜索的 id
     */
    private String searchId;
    /**
     * 是否搜索完成
     */
    private boolean finished;
    /**
     * 搜索图片的 id
     */
    private String imageId;
    /**
     * 搜索结果数
     */
    private int total;
    /**
     * 匹配到的结果列表
     */
    private List<CapturedPicture> pictures;

}
