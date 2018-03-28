package com.hzgc.service.staticrepo;


import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.service.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ChangeFeatureByPkey {
    private static Logger LOG = Logger.getLogger(ChangeFeatureByPkey.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOG.error("Wrong number of input parameters");
            System.exit(0);
        }
        String pKey = args[0];
        List<String[]> searchByKey = searchByKey(pKey);
        putData2HBase(searchByKey);
        putData2ES(searchByKey);
    }

    private static List<String[]> searchByKey(String pKey) {
        String feature = "";
        TransportClient esClient = ElasticSearchHelper.getEsClient();
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();

        //定义一个List用来存在查询得到的结果
        List<String[]> findResult = new ArrayList<>();
        //构造搜索对象
        SearchResponse searchResponse = esClient
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(6000))
                .setQuery(QueryBuilders.termQuery(ObjectInfoTable.PKEY, pKey))
                .setSize(100).get();
        do {
            //将结果进行封装
            SearchHits hits = searchResponse.getHits();
            //输出某个人员类型对应的记录条数
            SearchHit[] searchHits = hits.getHits();
            LOG.info("pkey为：" + pKey + "时，查询得到的记录数为：" + hits.getTotalHits());
            if (searchHits.length > 0) {
                for (SearchHit hit : searchHits) {
                    //得到每个人员类型对应的rowkey
                    String rowkey = hit.getId();
                    Get get = new Get(Bytes.toBytes(rowkey));
                    Result result = null;
                    try {
                        result = objectinfo.get(get);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] photo = result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.PHOTO));
                    //得到每个人员类型对应的特征值
                    feature = objectInfoHandler.getFeature(ObjectInfoTable.PERSON_COLF, photo);
                    //当有特征值时，才将结果返回
                    if (!feature.equals("")) {
                        //将人员类型、rowkey和特征值进行拼接
                        String[] result1 = new String[3];
                        result1[0] = rowkey;
                        result1[1] = pKey;
                        result1[2] = feature;
                        //将结果添加到集合中
                        findResult.add(result1);
                    }
                }
            }
            searchResponse = esClient.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(new TimeValue(6000)).execute().actionGet();
        } while (searchResponse.getHits().getHits().length != 0);
        return findResult;
    }

    private static void putData2HBase(List<String[]> searchByKey) {
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Iterator<String[]> iterator = searchByKey.iterator();
        try {
            while (iterator.hasNext()) {
                String[] result = iterator.next();
                String rowkey = result[0];
                String feature = result[2];
                LOG.info("put data rowkey is:" + rowkey + "to HBase");
                Put put = new Put(Bytes.toBytes(rowkey));
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(ObjectInfoTable.FEATURE), feature.getBytes("UTF-8"));
                objectinfo.put(put);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HBaseUtil.closTable(objectinfo);
        }

    }

    private static void putData2ES(List<String[]> searchByKey) {
        Iterator<String[]> iterator = searchByKey.iterator();
        TransportClient esClient = ElasticSearchHelper.getEsClient();
        try {
            while (iterator.hasNext()) {
                String[] result = iterator.next();
                String rowkey = result[0];
                String feature = result[2];
                LOG.info("UPdata rowkey is:" + rowkey + "to ES");
                UpdateRequest updateRequest = new UpdateRequest();
                updateRequest.index(ObjectInfoTable.TABLE_NAME);
                updateRequest.type(ObjectInfoTable.PERSON_COLF);
                updateRequest.id(rowkey);
                updateRequest.doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field(ObjectInfoTable.FEATURE, feature)
                        .endObject());
                UpdateResponse response = esClient.update(updateRequest).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            esClient.close();
        }
    }
}

