package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.CaptureNumberService;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.staticrepo.PhoenixJDBCHelper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class CaptureNumberImpl implements CaptureNumberService {

    /**
     * 查询es的动态库，返回总抓拍数量和今日抓拍数量
     * @param ipcId 设备ID：ipcId
     * @return 返回总抓拍数量和今日抓拍数量
     */
    @Override
    public synchronized Map<String, Integer> dynaicNumberService(List<String> ipcId) {
        String index = DynamicTable.DYNAMIC_INDEX;
        String type = DynamicTable.PERSON_INDEX_TYPE;
        Map<String, Integer> map = new HashMap<>();

        // 统计所有抓拍总数
        TransportClient client = ElasticSearchHelper.getEsClient();
        SearchResponse responseV1 = client.prepareSearch(index).setTypes(type)
                .setQuery(QueryBuilders.matchAllQuery()).setSize(1).get();
        int totalNumber = (int) responseV1.getHits().getTotalHits();
        map.put(totolNum, totalNumber);

        // 查询今天抓拍的数量
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String endTime = format.format(System.currentTimeMillis());
        SearchResponse responseV2 = client.prepareSearch(index)
                .setTypes(type)
                .setQuery(QueryBuilders
                        .matchPhraseQuery(DynamicTable.DATE,
                                endTime.substring(0, endTime.indexOf(" ")) ))
                .setSize(1)
                .get();
        int todayTotolNumber = (int) responseV2.getHits().getTotalHits();
        map.put(todyTotolNumber, todayTotolNumber);

        return map;
    }

    /**
     * 查询es的静态库，返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
     * @param platformId 平台ID
     * @return 返回每个平台下（对应platformId），每个对象库（对应pkey）下的人员的数量
     */
    @Override
    public synchronized Map<String, Integer> staticNumberService(String platformId) {
        Map<String, Integer> map = new HashMap<>();
        String sql = "select " + ObjectInfoTable.PKEY +", count(" + ObjectInfoTable.PKEY +") as countNum from " +
                ObjectInfoTable.TABLE_NAME + " where " + ObjectInfoTable.PLATFORMID + " = ?";
        Connection conn = PhoenixJDBCHelper.getInstance().getConnection();
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        try {
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, platformId);
            resultSet = pstm.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getString(ObjectInfoTable.PKEY), resultSet.getInt("countNum"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PhoenixJDBCHelper.closeConnection(null, pstm, resultSet);
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
    public synchronized Map<String, Integer> timeSoltNumber(List<String> ipcids, String startTime, String endTime) {
        List<String> times = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        BoolQueryBuilder totolQuery = QueryBuilders.boolQuery();
        if (ipcids != null && ipcids.size() > 0) {
            for (String ipcid : ipcids) {
                totolQuery.should(QueryBuilders.matchPhraseQuery("ipcid", ipcid));
            }
        }
        BoolQueryBuilder timeQuery = QueryBuilders.boolQuery();
        if (startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
            times = getHourTime(startTime, endTime);
            timeQuery.must(QueryBuilders.rangeQuery("time").gte(startTime).lte(endTime));
        }
        timeQuery.must(totolQuery);
        TransportClient client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder sbuilder = client.prepareSearch("dynamicshow").setTypes("person").setQuery(timeQuery);
        TermsAggregationBuilder timeagg = AggregationBuilders.terms("times").field("time").size(1000000);
        SumAggregationBuilder countagg = AggregationBuilders.sum("count_count").field("count");
        timeagg.subAggregation(countagg);
        sbuilder.addAggregation(timeagg);
        SearchResponse response = sbuilder.execute().actionGet();
        Map<String, Aggregation> aggMap = response.getAggregations().asMap();
        Terms time = (Terms) aggMap.get("times");
        Iterator<Terms.Bucket> teamBucket = (Iterator<Terms.Bucket>) time.getBuckets().iterator();
        for (String time1 : times) {
            map.put(time1, 0);
        }
        while (teamBucket.hasNext()) {
            Terms.Bucket buck = teamBucket.next();
            String timess = (String) buck.getKey();
            Map<String, Aggregation> subagg = buck.getAggregations().asMap();
            int count_count = (int) ((InternalSum) subagg.get("count_count")).getValue();
            map.put(timess, count_count);
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
