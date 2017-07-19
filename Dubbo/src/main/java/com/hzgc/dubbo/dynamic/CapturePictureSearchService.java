package com.hzgc.dubbo.dynamic;

import java.util.Map;

/**
 * 图片搜索服务
 */
public interface CapturePictureSearchService {
    /**
     * @param option 搜索选项
     * @return 搜索结果 id
     */
    //如果大数据处理的时间过长，则先返回searchId,finished=false,然后再开始计算
    SearchResult search(SearchOption option);

    /**
     * @param searchId 搜索的 id
     * @param offset   从第几条开始
     * @param count    条数
     * @return 结果
     */

    SearchResult getSearchResult(String searchId, int offset, int count);


    /**
     * 查看图片有哪些属性
     *
     * @param type 类型
     * @return 过滤参数键值对
     */
    Map<String, String> getSearchFilterParams(int type);

    /**
     * 根据id获取原图
     *
     * @param imageId
     * @param type
     * @return
     */
    byte[] getPicture(String imageId, int type);
}
