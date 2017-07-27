package com.hzgc.dubbo.dynamicrepo;

import java.util.Map;

/**
 * 以图搜图接口，内含五个方法（外）（彭聪）
 */
public interface CapturePictureSearchService {
    /**
     * 接收应用层传递的参数进行搜图，如果大数据处理的时间过长，
     * 则先返回searchId,finished=false,然后再开始计算；如果能够在秒级时间内计算完则计算完后再返回结果
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    SearchResult search(SearchOption option);

    /**
     * @param searchId 搜索的 id（rowkey）
     * @param offset   从第几条开始
     * @param count    条数
     * @return SearchResult对象
     */

    SearchResult getSearchResult(String searchId, int offset, int count);

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 过滤参数键值对
     */
    Map<String, String> getSearchFilterParams(int type);

    /**
     * 根据id（rowkey）获取原图
     *
     * @param imageId rowkey
     * @param type    图片类型，人还是车
     * @return 以二进制数组的形式返回图片
     */
    byte[] getPicture(String imageId, PictureType type);

    /**
     * 根据id（rowkey）获取PersonPhoto对象
     *
     * @param imageId   rowkey
     * @param type  图片类型，人还是车
     * @return  DynamicObject对象
     */
    public DynamicObject getCaptureMessage(String imageId, int type);
}
