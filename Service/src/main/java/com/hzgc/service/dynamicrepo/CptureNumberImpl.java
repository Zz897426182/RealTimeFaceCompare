package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.CaptureNumberService;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 这个方法是为了大数据可视化而指定的CaptureNumberService，继承于，主要包含三个方法：
 * 1、dynaicNumberService：查询es的动态库，返回总抓拍数量和今日抓拍数量
 * 2、staticNumberService：查询es的静态库，返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
 * 3、timeSoltNumber：根据入参ipcid的list、startTime和endTime去es查询到相应的值
 */
public class CptureNumberImpl implements CaptureNumberService {

    /**
     * 查询es的动态库，返回总抓拍数量和今日抓拍数量
     * @param ipcId 设备ID：ipcId
     * @return 返回总抓拍数量和今日抓拍数量
     */
    @Override
    public Map<String, Integer> dynaicNumberService(List<String> ipcId) {
        String index = DynamicTable.DYNAMIC_INDEX;
        String type = DynamicTable.PERSON_INDEX_TYPE;
        Map<String, Integer> map = new HashMap<>();
        BoolQueryBuilder totolBQ = QueryBuilders.boolQuery();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long a = System.currentTimeMillis();
        String endTime = format.format(a);
        String startTime = endTime.substring(0, endTime.indexOf(" ")) + " 00:00:00";
        totolBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime));
        BoolQueryBuilder ipcBQ = QueryBuilders.boolQuery();
        if (ipcId != null) {
            for (String ipcid : ipcId) {
                ipcBQ.should(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcid));
            }
            totolBQ.must(ipcBQ);
        }
        TransportClient client = ElasticSearchHelper.getEsClient();
        SearchResponse searchResponse1 = client.prepareSearch(index)
                .setTypes(type)
                .setSize(1)
                .get();
        SearchHits searchHits1 = searchResponse1.getHits();
        int totolNumber = (int) searchHits1.getTotalHits();
        SearchResponse searchResponse2 = client.prepareSearch(index)
                .setTypes(type)
                .setQuery(totolBQ)
                .setSize(1)
                .get();
        SearchHits searchHits2 = searchResponse2.getHits();
        int todayTotolNumber = (int) searchHits2.getTotalHits();
        map.put(totolNum, totolNumber);
        map.put(todyTotolNumber, todayTotolNumber);
        return map;
    }

    /**
     * 查询es的静态库，返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
     * @param platformId 平台ID
     * @return 返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
     */
    @Override
    public Map<String, Integer> staticNumberService(String platformId) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String index = ObjectInfoTable.TABLE_NAME;
        String type = ObjectInfoTable.PERSON_COLF;
        Map<String, Integer> map = new HashMap<>();
        if (platformId != null) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(ObjectInfoTable.PLATFORMID, platformId));
        }
        SearchRequestBuilder searchRequestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(index)
                .setTypes(type)
                .setQuery(boolQueryBuilder);
        TermsAggregationBuilder tamAgg = AggregationBuilders.terms("pkey_count").field("pkey");
        searchRequestBuilder.addAggregation(tamAgg);
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        Terms pkey_count = response.getAggregations().get("pkey_count");
        for (Terms.Bucket bk : pkey_count.getBuckets()) {
            String pkey = (String) bk.getKey();
            int pkeyNumber = (int) bk.getDocCount();
            map.put(pkey, pkeyNumber);
        }
        return map;
    }

    /**
     * 根据入参ipcid的list、startTime和endTime去es查询到相应的值
     * @param ipcids 设备ID：ipcid
     * @param startTime 搜索的开始时间
     * @param endTime 搜索的结束时间
     * @return 返回某段时间内，这些ipcid的抓拍的总数量
     */
    @Override
    public Map<String, Integer> timeSoltNumber(List<String> ipcids, String startTime, String endTime) {
        List<String> times = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        BoolQueryBuilder totolQuery = QueryBuilders.boolQuery();
        if (ipcids != null && ipcids.size() > 0) {
            for (String ipcid : ipcids){
                totolQuery.should(QueryBuilders.matchPhraseQuery("ipcid", ipcid));
            }
        }
        BoolQueryBuilder timeQuery = QueryBuilders.boolQuery();
        if (startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
            times = getHourTime(startTime, endTime);
            timeQuery.must(QueryBuilders.rangeQuery("time").gte(startTime).lte(endTime));
        }
        timeQuery.must(totolQuery);
        SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                .prepareSearch("dynamicshow")
                .setTypes("person")
                .setQuery(timeQuery)
                .setSize(100000000)
                .get();
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();
        if (times != null && times.size() > 0) {
            for (String time : times) {
                int count = 0;
                for (SearchHit hit : hits) {
                    String actTime = (String) hit.getSource().get("time");
                    int actCount = (int) hit.getSource().get("count");
                    if (Objects.equals(actTime, time)) {
                        count += actCount;
                    }
                }
                map.put(time, count);
            }
        }
        return map;
    }

    /**
     * 通过入参确定起始和截止的时间，返回这段时间内的每一个小时的String
     *
     * @param startTime 开始时间
     * @param endTime   截止时间
     * @return 返回这段时间内的每一个小时的String
     */
    private List<String> getHourTime(String startTime, String endTime) {
        List<String> timeList = new ArrayList<>();
        Calendar start = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            start.setTime(df.parse(startTime));
            Long startTimeL = start.getTimeInMillis();
            Calendar end = Calendar.getInstance();
            end.setTime(df.parse(endTime));
            Long endTimeL = end.getTimeInMillis();
            Long onehour = 1000 * 60 * 60L;
            Long time = startTimeL;
            while (time <= endTimeL) {
                Date everyTime = new Date(time);
                String timee = df.format(everyTime);
                timeList.add(timee);
                time += onehour;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeList;
    }
}
