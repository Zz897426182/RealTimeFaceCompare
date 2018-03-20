package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.attribute.AttributeValue;
import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.FtpUtils;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

class CaptureHistory {
    private static Logger LOG = Logger.getLogger(CaptureHistory.class);

    static {
        ElasticSearchHelper.getEsClient();
    }

    List<SearchResult> getRowKey_history(SearchOption option, List<String> ipcId, List<SortParam> sortParams) {
        SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder_history(option);
        return dealWithSearchRequestBuilder_history(searchRequestBuilder, ipcId, sortParams,option);
    }

    private SearchRequestBuilder getSearchRequestBuilder_history(SearchOption option) {
        // 传过来为空，返回空
        if (option == null) {
            return null;
        }
        // 获取搜索类型，搜索类型要么是人，要么是车，不可以为空，为空不处理
        SearchType searchType = option.getSearchType();
        // 搜索类型为空，则返回空。
        if (searchType == null) {
            return null;
        }

        // es 中的索引，
        String index = "";
        // es 中类型
        String type = "";
        // 最终封装成的boolQueryBuilder 对象。
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();

        int offset = option.getOffset();
        LOG.info("offset is:" + offset);
        int count = option.getCount();
        LOG.info("count is:" + count);
        //排序条件
        List<SortParam> sortParams = option.getSortParams();
        String px = "desc";
        for (SortParam s : sortParams) {
            if (s.name().equals("TIMEDESC")) {
                px = "desc";
            } else if (s.name().equals("TIMEASC")) {
                px = "asc";
            }
        }

        // 搜索类型为人的情况下
        if (SearchType.PERSON.equals(searchType)) {
            // 获取设备ID
            List<String> deviceId = option.getDeviceIds();
            // 起始时间
            String startTime = option.getStartDate();
            // 结束时间
            String endTime = option.getEndDate();
            // 时间段
            List<TimeInterval> timeIntervals = option.getIntervals();
            //人脸属性
            List<Attribute> attributes = option.getAttributes();
            //筛选人脸属性
            if (attributes != null) {
                for (Attribute attribute : attributes) {
                    String identify = attribute.getIdentify().toLowerCase();
                    String logic = String.valueOf(attribute.getLogistic());
                    List<AttributeValue> attributeValues = attribute.getValues();
                    for (AttributeValue attributeValue : attributeValues) {
                        int attr = attributeValue.getValue();
                        if (logic.equals("OR")) {
                            totalBQ.should(QueryBuilders.matchQuery(identify, attr).analyzer("standard"));
                        } else {
                            totalBQ.must(QueryBuilders.matchQuery(identify, attr).analyzer("standard"));
                        }
                    }
                }
            }
            // 设备ID 的的boolQueryBuilder
            BoolQueryBuilder devicdIdBQ = QueryBuilders.boolQuery();
            // 设备ID 存在的时候的处理
            if (deviceId != null) {
                for (Object t : deviceId) {
                    devicdIdBQ.should(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, t).analyzer("standard"));
                }
                totalBQ.must(devicdIdBQ);
            }
            // 开始时间和结束时间存在的时候的处理
            if (startTime != null && endTime != null && !startTime.equals("") && !endTime.equals("")) {
                totalBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime).lte(endTime));
            }
            //TimeIntervals 时间段的封装类
            TimeInterval timeInterval;
            // 时间段的BoolQueryBuilder
            BoolQueryBuilder timeInQB = QueryBuilders.boolQuery();
            // 对时间段的处理
            if (timeIntervals != null) {
                for (TimeInterval timeInterval1 : timeIntervals) {
                    timeInterval = timeInterval1;
                    int start_sj = timeInterval.getStart();
                    String start_ts = String.valueOf(start_sj * 100 / 60 + start_sj % 60);
                    int end_sj = timeInterval.getEnd();
                    String end_ts = String.valueOf(end_sj * 100 / 60 + end_sj % 60);
                    timeInQB.should(QueryBuilders.rangeQuery(DynamicTable.TIMESLOT).gte(start_ts).lte(end_ts));
                    totalBQ.must(timeInQB);
                }
            }
            //索引和类型
            index = DynamicTable.DYNAMIC_INDEX;
            type = DynamicTable.PERSON_INDEX_TYPE;
        } else if (SearchType.CAR.equals(searchType)) {     // 搜索的是车的情况下

        }
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(index)
                .setTypes(type)
                .addSort("exacttime", SortOrder.fromString(px));
        return requestBuilder.setQuery(totalBQ);
    }


    private List<SearchResult> dealWithSearchRequestBuilder_history
            (SearchRequestBuilder searchRequestBuilder, List<String> ipcId, List<SortParam> sortParams,SearchOption option) {
        // 最终要返回的值
        List<SearchResult> resultList = new ArrayList<>();
        // requestBuilder 为空，则返回空
        if (ipcId != null && ipcId.size() > 0 && sortParams.get(0).name().equals("IPC")) {
            if (searchRequestBuilder == null) {
                return resultList;
            }
            for (String ipcid : ipcId) {
                int offset = option.getOffset();
                LOG.info("offset is:" + offset);
                int count = option.getCount();
                LOG.info("count is:" + count);
                BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
                totalBQ.must(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcid).analyzer("standard"));
                searchRequestBuilder.setQuery(totalBQ).setFrom(offset).setSize(count);
                SearchResult result = new SearchResult();
                List<SingleResult> results = new ArrayList<>();
                SingleResult singleResult = new SingleResult();
                List<GroupByIpc> picturesByIpc = new ArrayList<>();
                GroupByIpc groupByIpc = new GroupByIpc();
                SearchResponse searchResponse = searchRequestBuilder.get();
                SearchHits searchHits = searchResponse.getHits();
                SearchHit[] hits = searchHits.getHits();
                List<CapturedPicture> persons = new ArrayList<>();
                CapturedPicture capturePicture;
                if (hits.length > 0) {
                    for (SearchHit hit : hits) {
                        capturePicture = new CapturedPicture();
                        String surl = hit.getId();
                        String burl = FtpUtils.surlToBurl(surl);
                        String ipc = (String) hit.getSource().get(DynamicTable.IPCID);
                        String timestamp = (String) hit.getSource().get(DynamicTable.TIMESTAMP);
                        capturePicture.setSurl(FtpUtils.getFtpUrl(surl));
                        capturePicture.setBurl(FtpUtils.getFtpUrl(burl));
                        capturePicture.setIpcId(ipc);
                        capturePicture.setTimeStamp(timestamp);
                        if (ipcid.equals(ipc)) {
                            groupByIpc.setIpc(ipc);
                            picturesByIpc.add(groupByIpc);
                            persons.add(capturePicture);
                        }
                    }
                    singleResult.setTotal(persons.size());
                    singleResult.setPicturesByIpc(picturesByIpc);
                    singleResult.setPictures(persons);
                    results.add(singleResult);
                    result.setResults(results);
                }
                resultList.add(result);
            }
        } else if (ipcId != null && ipcId.size() > 0 && !sortParams.get(0).name().equals("IPC")) {
            if (searchRequestBuilder == null) {
                return resultList;
            }
            SearchResult result = new SearchResult();
            List<SingleResult> results = new ArrayList<>();
            SingleResult singleResult = new SingleResult();
            List<CapturedPicture> persons = new ArrayList<>();
            int offset = option.getOffset();
            LOG.info("offset is:" + offset);
            int count = option.getCount();
            LOG.info("count is:" + count);
            searchRequestBuilder.setFrom(offset).setSize(count);
            for (String ipcid : ipcId) {
                SearchResponse searchResponse = searchRequestBuilder.get();
                SearchHits searchHits = searchResponse.getHits();
                SearchHit[] hits = searchHits.getHits();
                CapturedPicture capturePicture;
                if (hits.length > 0) {
                    for (SearchHit hit : hits) {
                        capturePicture = new CapturedPicture();
                        String surl = hit.getId();
                        String burl = FtpUtils.surlToBurl(surl);
                        String ipc = (String) hit.getSource().get(DynamicTable.IPCID);
                        String timestamp = (String) hit.getSource().get(DynamicTable.TIMESTAMP);
                        capturePicture.setSurl(FtpUtils.getFtpUrl(surl));
                        capturePicture.setBurl(FtpUtils.getFtpUrl(burl));
                        capturePicture.setIpcId(ipc);
                        capturePicture.setTimeStamp(timestamp);
                        if (ipcid.equals(ipc)) {
                            persons.add(capturePicture);
                        }
                    }
                }
            }
            singleResult.setTotal(persons.size());
            singleResult.setPictures(persons);
            results.add(singleResult);
            result.setResults(results);
            resultList.add(result);
        } else if ((ipcId == null || ipcId.size() == 0) && !sortParams.get(0).name().equals("IPC")) {
            SearchResult result = new SearchResult();
            List<SingleResult> results = new ArrayList<>();
            SingleResult singleResult = new SingleResult();
            int offset = option.getOffset();
            LOG.info("offset is:" + offset);
            int count = option.getCount();
            LOG.info("count is:" + count);
            searchRequestBuilder.setFrom(offset).setSize(count);
            if (searchRequestBuilder == null) {
                return resultList;
            }
            // 通过SearchRequestBuilder 获取response 对象。
            SearchResponse searchResponse = searchRequestBuilder.get();
            // 滚动查询
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] hits = searchHits.getHits();
            System.out.println(hits.length);
            List<CapturedPicture> persons = new ArrayList<>();
            CapturedPicture capturePicture;
            if (hits.length > 0) {
                for (SearchHit hit : hits) {
                    capturePicture = new CapturedPicture();
                    String surl = hit.getId();
                    String burl = FtpUtils.surlToBurl(surl);
                    String ipcid = (String) hit.getSource().get(DynamicTable.IPCID);
                    String timestamp = (String) hit.getSource().get(DynamicTable.TIMESTAMP);
                    capturePicture.setSurl(FtpUtils.getFtpUrl(surl));
                    capturePicture.setBurl(FtpUtils.getFtpUrl(burl));
                    capturePicture.setIpcId(ipcid);
                    capturePicture.setTimeStamp(timestamp);
                    persons.add(capturePicture);
                }
            }
            singleResult.setTotal(persons.size());
            singleResult.setPictures(persons);
            results.add(singleResult);
            result.setResults(results);
            resultList.add(result);
        }
        return resultList;
    }
}