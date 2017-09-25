package com.hzgc.dubbo.dynamicrepo;

import java.util.List;
import java.util.Map;

/**
 * 以图搜图接口，内含四个方法（外）（彭聪）
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
     * 查询历史记录
     *
     * @param searchId   搜索的 id（rowkey）
     * @param offset     从第几条开始
     * @param count      条数
     * @param sortParams 排序参数
     * @return SearchResult对象
     */
    SearchResult getSearchResult(String searchId, int offset, int count, String sortParams);

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 过滤参数键值对
     */
    Map<String, String> getSearchFilterParams(int type);

    /**
     * 根据id（rowkey）获取动态信息库内容（CapturedPicture对象）（刘思阳）
     *
     * @param imageId id（小图rowkey）
     * @param type    图片类型，人/车
     * @return CapturedPicture    动态库对象
     */
    CapturedPicture getCaptureMessage(String imageId, int type);

    /**
     * 批量获取图片
     *
     * @param imageIdList 图片Id列表
     * @param type        类型
     * @return 图片列表
     */
    List<CapturedPicture> getBatchCaptureMessage(List<String> imageIdList, int type);

}
