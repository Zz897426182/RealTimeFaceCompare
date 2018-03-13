package com.hzgc.service.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class ObjectInfoInnerHandlerImpl implements ObjectInfoInnerHandler, Serializable {

    private static Logger LOG = Logger.getLogger(ObjectInfoInnerHandlerImpl.class);
    private static ObjectInfoInnerHandlerImpl instance;
    private static long totalNums = getTotalNums();
    private static List<Object[]> totalList = null;

    /**
     * 接口实现使用单例模式
     */
    private ObjectInfoInnerHandlerImpl() {
        ElasticSearchHelper.getEsClient();
    }

    /**
     * 获取内存中底库数据
     *
     * @return 返回底库
     */
    public List<Object[]> getTotalList() {
        if (totalList == null || totalNumIsChange()) {
            System.out.println("Start load static library...");
            totalList = searchByPkeys();
            System.out.println("Load static library successfull...");
            return totalList;
        } else {
            return totalList;
        }
    }

    /**
     * 用于判断HBase 中的静态信息库数据量是否有改变
     *
     * @return true表示有变化, false 表示没有变化
     */
    private boolean totalNumIsChange() {
        long newTotalNums = getTotalNums();
        if (totalNums == newTotalNums) {
            return false;
        } else {
            System.out.println("TotalNums changed");
            setTotalNums(newTotalNums);
            return true;
        }
    }

    /**
     * 获取对象的唯一方法
     *
     * @return 返回实例化对象
     */
    public static ObjectInfoInnerHandlerImpl getInstance() {
        if (instance == null) {
            synchronized (ObjectInfoInnerHandlerImpl.class) {
                if (instance == null) {
                    instance = new ObjectInfoInnerHandlerImpl();
                }
            }
        }
        return instance;
    }

    /**
     * 设置当前底库总数
     *
     * @param newToalNums 底库中最新总数信息
     */
    private static void setTotalNums(long newToalNums) {
        totalNums = newToalNums;
        LOG.info("set new number successfull, new number is:" + totalNums);
    }

    /**
     * 获取底库中最新总数信息
     *
     * @return 最新总数信息
     */
    private static long getTotalNums() {
        Get get = new Get(Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS_ROW_NAME));
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        try {
            Result result = table.get(get);
            return Bytes.toLong(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                    Bytes.toBytes(ObjectInfoTable.TOTAL_NUMS)));
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 查询所有的数据
     *
     * @return 返回其中的rowkey, pkey, feature
     */
    private List<Object[]> searchByPkeys() {
        List<Object[]> findResult = new ArrayList<>();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Scan scan = new Scan();
        scan.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.FEATURE));
        scan.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PKEY));
        try {
            ResultScanner resultScanner = objectinfo.getScanner(scan);
            for (Result result : resultScanner) {
                String rowKey = Bytes.toString(result.getRow());
                String pkey = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY)));
                byte[] feature = result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE));
                if (null != feature && feature.length > 0) {
                    //将人员类型rowkey和特征值进行拼接
                    String feature_str = new String(feature, "ISO8859-1");
                    Object[] result1 = new Object[3];
                    result1[0] = rowKey;
                    result1[1] = pkey;
                    result1[2] = FaceFunction.string2floatArray(feature_str);
                    //将结果添加到集合中
                    findResult.add(result1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtil.closTable(objectinfo);
        }
        return findResult;
    }

    /**
     * 根据pkey的List来进行返回符合条件的数据
     *
     * @param pkeys 人员类型keys列表
     * @return 符合条件的List
     */
    @Override
    public List<String[]> searchByPkeys(List<String> pkeys) {
        if (pkeys == null) {
            return null;
        }
        //遍历人员类型
        Iterator it = pkeys.iterator();
        //构造搜索对象
        SearchResponse searchResponse;
        //定义一个List用来存在查询得到的结果
        List<String[]> findResult = new ArrayList<>();
        //设置搜索条件
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        while (it.hasNext()) {
            //取出遍历的值
            String pkey = (String) it.next();
            //根据遍历得到的人员类型进行精确查询
            requestBuilder.setQuery(QueryBuilders.termQuery(ObjectInfoTable.PKEY, pkey));
            //通过requestBuilder的get方法执行查询任务
            searchResponse = requestBuilder.get();
            do {
                //将结果进行封装
                SearchHits hits = searchResponse.getHits();
                //输出某个人员类型对应的记录条数
                SearchHit[] searchHits = hits.getHits();
                System.out.println("pkey为：" + pkey + "时，查询得到的记录数为：" + hits.getTotalHits());
                if (searchHits.length > 0) {
                    for (SearchHit hit : searchHits) {
                        //得到每个人员类型对应的rowkey
                        String id = hit.getId();
                        //得到每个人员类型对应的特征值
                        Map<String, Object> sourceList = hit.getSource();
                        String feature = (String) sourceList.get("feature");
                        //当有特征值时，才将结果返回
                        if (null != feature) {
                            //将人员类型、rowkey和特征值进行拼接
                            String[] result = new String[3];
                            result[0] = id;
                            result[1] = pkey;
                            result[2] = feature;
                            //将结果添加到集合中
                            findResult.add(result);
                        }
                    }
                }
                searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(new TimeValue(6000))
                        .execute()
                        .actionGet();
            } while (searchResponse.getHits().getHits().length != 0);
        }
        return findResult;
    }

    /**
     * 获取底库中所有信息的最近一次出现时间
     *
     * @return 返回符合条件的数据
     */
    public List<String> searchByPkeysUpdateTime() {
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = matchAllQuery();
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(qb)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        SearchResponse searchResponse = requestBuilder.get();
        do {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            if (searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    //得到每个人员类型对应的rowkey
                    String id = hit.getId();
                    //得到每个人员类型对应的特征值
                    Map<String, Object> sourceList = hit.getSource();
                    String updatetime = (String) sourceList.get("updatetime");
                    String pkey = (String) sourceList.get("pkey");
                    //将人员类型、rowkey和特征值进行拼接
                    String result = id + "ZHONGXIAN" + pkey + "ZHONGXIAN" + updatetime;
                    //将结果添加到集合中
                    findResult.add(result);
                }
            }
            searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(6000))
                    .execute()
                    .actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        return findResult;
    }

    /**
     * 获取底库中包含在此List中的人员信息及最新出现时间
     *
     * @param pkeys 对象类型列表
     * @return 返回符合条件的数据
     */
    public List<String> searchByPkeysUpdateTime(List<String> pkeys) {
        List<String> findResult = new ArrayList<>();
        QueryBuilder qb = QueryBuilders.termsQuery(ObjectInfoTable.PKEY, pkeys);
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(qb)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setSize(1000)
                .setExplain(true);
        SearchResponse searchResponse = requestBuilder.get();
        do {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            if (searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    //得到每个人员类型对应的rowkey
                    String id = hit.getId();
                    //得到每个人员类型对应的特征值
                    Map<String, Object> sourceList = hit.getSource();
                    String updatetime = (String) sourceList.get("updatetime");
                    String pkey = (String) sourceList.get("pkey");
                    //将人员类型、rowkey和特征值进行拼接
                    String result = id + "ZHONGXIAN" + pkey + "ZHONGXIAN" + updatetime;
                    //将结果添加到集合中
                    findResult.add(result);
                }
            }
            searchResponse = ElasticSearchHelper.getEsClient().prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(6000))
                    .execute()
                    .actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        return findResult;
    }

    /**
     * 更新此List中的人员最新出现时间
     *
     * @param rowkeys 对象类型列表
     * @return 0成功, 1失败
     */
    public int updateObjectInfoTime(List<String> rowkeys) {
        if (rowkeys == null || rowkeys.size() <= 0) {
            return 0;
        }
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        List<Put> puts = new ArrayList<>();
        try {
            for (int i = 0; i < rowkeys.size(); i++) {
                Put put = new Put(Bytes.toBytes(rowkeys.get(i)));
                put.setDurability(Durability.ASYNC_WAL);
                // 获取系统当前时间
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = format.format(date);
                // 构造一个更新对象信息中的更新时间段的put
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
                puts.add(put);
                if (i % 10000 == 0) {
                    table.put(puts);
                    puts.clear();
                }
            }
            if (puts.size() > 0) {
                table.put(puts);
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        } finally {
            LOG.info("offline alarm time is updated successfully");
            HBaseUtil.closTable(table);
        }
    }
}
