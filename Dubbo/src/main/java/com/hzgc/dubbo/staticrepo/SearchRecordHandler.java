package com.hzgc.dubbo.staticrepo;

public interface SearchRecordHandler {
    /**
     * @param rowkey，即Hbase 数据库中的rowkey，查询记录唯一标志（外） （李第亮）
     * @param start  返回的查询记录中，从哪一条开始
     * @param pageSize  需要返回的记录数
     * @return  返回一个ObjectSearchResult 对象，
     * 里面包含了本次查询ID，查询成功标识，
     * 查询照片ID（无照片，此参数为空），结果数，人员信息列表
     */
    public ObjectSearchResult getRocordOfObjectInfo(String rowkey, long start, long pageSize);

    /**
     *
     * @param rowkey 即Hbase 数据库中的rowkey，查询记录唯一标志 （外） （李第亮）
     * @return 返回查询的照片
     */
    public byte[] getSearchPhoto(String rowkey);

}
