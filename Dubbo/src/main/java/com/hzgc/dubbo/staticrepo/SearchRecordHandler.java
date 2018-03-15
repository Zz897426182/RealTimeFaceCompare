package com.hzgc.dubbo.staticrepo;

/**
 * 用图片搜索静态信息库的历史记录查询接口
 * @author 李第亮
 */
public interface SearchRecordHandler {
    /**
     * 根据传过来的搜索rowkey 返回搜索记录 （外） （李第亮）
     * @param  searchRecordOpts 历史查询参数
     * @return  返回一个ObjectSearchResult 对象，
     * @author 李第亮
     * 里面包含了本次查询ID，查询成功标识，
     * 查询照片ID（无照片，此参数为空），结果数，人员信息列表
     */
    ObjectSearchResult getRocordOfObjectInfo(SearchRecordOpts searchRecordOpts);

    /**
     * 根据穿过来的rowkey 返回照片 （外） （李第亮）
     * @param rowkey 即Hbase 数据库中的rowkey，查询记录唯一标志
     * @return 返回查询的照片
     */
    byte[] getSearchPhoto(String rowkey);
}
