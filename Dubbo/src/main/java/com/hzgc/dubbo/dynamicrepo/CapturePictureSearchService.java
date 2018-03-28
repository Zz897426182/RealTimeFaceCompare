package com.hzgc.dubbo.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.attribute.AttributeCount;

import java.util.List;

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
     * @return 属性对象列表
     */
    List<Attribute> getAttribute(SearchType type);

    /**
     * 抓拍统计查询接口（马燊偲）
     * 查询指定时间段内，指定设备抓拍的图片数量、该设备最后一次抓拍时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipcId     设备ID
     * @return CaptureCount 查询结果对象。对象内封装了：该时间段内该设备抓拍张数，该时间段内该设备最后一次抓拍时间。
     */
    CaptureCount captureCountQuery(String startTime, String endTime, String ipcId);

    /**
     * 查询抓拍历史记录（陈柯）
     * 根据条件筛选抓拍图片，并返回图片对象
     * @param option option中包含count、时间段、时间戳、人脸属性等值，根据这些值去筛选
     *               符合条件的图片对象并返回
     * @return SearchResult符合条件的图片对象
     */
    SearchResult getCaptureHistory(SearchOption option);

    /**
     * 抓拍属性统计查询 (刘思阳)
     * 查询指定时间段内，单个或某组设备中某种属性在抓拍图片中的数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipcIdList 单个或某组设备ID
     * @param type      统计类型
     * @return 单个或某组设备中某种属性在抓拍图片中的数量
     */
    List<AttributeCount> captureAttributeQuery(String startTime, String endTime, List<String> ipcIdList, SearchType type);

}
