package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.attribute.*;
import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.jni.NativeFunction;
import com.hzgc.util.common.UuidUtil;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.util.*;

/**
 * 以图搜图接口实现类，内含四个方法（外）（彭聪）
 */
public class CapturePictureSearchServiceImpl implements CapturePictureSearchService {
    private static Logger LOG = Logger.getLogger(CapturePictureSearchServiceImpl.class);

    static {
        ElasticSearchHelper.getEsClient();
        HBaseHelper.getHBaseConnection();
//        NativeFunction.init();
    }

    /**
     * 接收应用层传递的参数进行搜图（乔凯峰）
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    @Override
    public SearchResult search(SearchOption option) {
        long start = System.currentTimeMillis();
        SearchResult searchResult = null;
        RealTimeFaceCompareBySparkSQL realTimeFaceCompareBySparkSQL;
        if (null != option) {
            realTimeFaceCompareBySparkSQL = new RealTimeFaceCompareBySparkSQL();
            //搜索类型 是人还是车
            //设置查询Id
            String searchId = UuidUtil.setUuid();
            LOG.info("Generate current query id:[" + searchId + "]");
            //查询的对象库是人
            if (option.getImages() != null && option.getImages().size() > 0) {
                if (option.getThreshold() != 0.0) {
                    //根据上传的图片查询
                    searchResult = realTimeFaceCompareBySparkSQL.pictureSearchBySparkSQL(option, searchId);
                } else {
                    LOG.warn("The threshold is null");
                }
            } else {
                return null;
            }
        } else {
            LOG.error("Search parameter option is null");
        }
        LOG.info("Search total time is:" + (System.currentTimeMillis() - start));
        return searchResult;
    }

    /**
     * @param resultOption 历史结果查询参数对象
     * @return SearchResult对象
     */
    @Override
    public SearchResult getSearchResult(SearchResultOption resultOption) {
        SearchResult searchResult = null;
        if (resultOption.getSearchID() != null && !"".equals(resultOption.getSearchID())) {
            searchResult = DynamicPhotoServiceHelper.getSearchRes(resultOption.getSearchID());
            if (searchResult != null) {
                switch (searchResult.getSearchType()) {
                    case DynamicTable.PERSON_TYPE:
                        if (resultOption.getSortParam() != null && resultOption.getSortParam().size() > 0) {
                            DynamicPhotoServiceHelper.sortByParamsAndPageSplit(searchResult, resultOption);
                        } else {
                            for (SingleResult singleResult : searchResult.getResults()) {
                                DynamicPhotoServiceHelper.pageSplit(singleResult.getPictures(), resultOption);
                            }
                        }
                        break;
                    case DynamicTable.CAR_TYPE:
                        LOG.error("No vehicle queries are currently supported");
                        break;
                    default:
                        for (SingleResult singleResult : searchResult.getResults()) {
                            DynamicPhotoServiceHelper.pageSplit(singleResult.getPictures(), resultOption);
                        }
                }
            } else {
                LOG.error("Get query history failure");
            }

        } else {
            LOG.info("SearchId is null");
        }
        return searchResult;
    }

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 属性对象列表
     */
    @Override
    public List<Attribute> getAttribute(SearchType type) {
        List<Attribute> attributeList = new ArrayList<>();
        if (type == SearchType.PERSON) {
            Attribute hairColor = new Attribute();
            hairColor.setIdentify(HairColor.class.getSimpleName());
            hairColor.setDesc("发色");
            hairColor.setLogistic(Logistic.OR);
            List<AttributeValue> hairColorValueList = new ArrayList<>();
            for (HairColor hc : HairColor.values()) {
                AttributeValue hairColorValue = new AttributeValue();
                hairColorValue.setValue(hc.ordinal());
                hairColorValue.setDesc(HairColor.getDesc(hc));
                hairColorValueList.add(hairColorValue);
            }
            hairColor.setValues(hairColorValueList);
            attributeList.add(hairColor);

            Attribute hairStyle = new Attribute();
            hairStyle.setIdentify(HairStyle.class.getSimpleName());
            hairStyle.setDesc("发型");
            hairStyle.setLogistic(Logistic.OR);
            List<AttributeValue> hairStyleValueList = new ArrayList<>();
            for (HairStyle hs : HairStyle.values()) {
                AttributeValue hairStyleValue = new AttributeValue();
                hairStyleValue.setValue(hs.ordinal());
                hairStyleValue.setDesc(HairStyle.getDesc(hs));
                hairStyleValueList.add(hairStyleValue);
            }
            hairStyle.setValues(hairStyleValueList);
            attributeList.add(hairStyle);

            Attribute gender = new Attribute();
            gender.setIdentify(Gender.class.getSimpleName());
            gender.setDesc("性别");
            gender.setLogistic(Logistic.OR);
            List<AttributeValue> genderValueList = new ArrayList<>();
            for (Gender gend : Gender.values()) {
                AttributeValue genderValue = new AttributeValue();
                genderValue.setValue(gend.ordinal());
                genderValue.setDesc(Gender.getDesc(gend));
                genderValueList.add(genderValue);
            }
            gender.setValues(genderValueList);
            attributeList.add(gender);

            Attribute hat = new Attribute();
            hat.setIdentify(Hat.class.getSimpleName());
            hat.setDesc("帽子");
            hat.setLogistic(Logistic.OR);
            List<AttributeValue> hatValueList = new ArrayList<>();
            for (Hat h : Hat.values()) {
                AttributeValue hatValue = new AttributeValue();
                hatValue.setValue(h.ordinal());
                hatValue.setDesc(Hat.getDesc(h));
                hatValueList.add(hatValue);
            }
            hat.setValues(hatValueList);
            attributeList.add(hat);

            Attribute tie = new Attribute();
            tie.setIdentify(Tie.class.getSimpleName());
            tie.setDesc("领带");
            tie.setLogistic(Logistic.OR);
            List<AttributeValue> tieValueList = new ArrayList<>();
            for (Tie t : Tie.values()) {
                AttributeValue tieValue = new AttributeValue();
                tieValue.setValue(t.ordinal());
                tieValue.setDesc(Tie.getDesc(t));
                tieValueList.add(tieValue);
            }
            tie.setValues(tieValueList);
            attributeList.add(tie);

            Attribute huzi = new Attribute();
            huzi.setIdentify(Huzi.class.getSimpleName());
            huzi.setDesc("胡子");
            huzi.setLogistic(Logistic.OR);
            List<AttributeValue> huziValueList = new ArrayList<>();
            for (Huzi hz : Huzi.values()) {
                AttributeValue huziValue = new AttributeValue();
                huziValue.setValue(hz.ordinal());
                huziValue.setDesc(Huzi.getDesc(hz));
                huziValueList.add(huziValue);
            }
            huzi.setValues(huziValueList);
            attributeList.add(huzi);

            Attribute eyeglasses = new Attribute();
            eyeglasses.setIdentify(Eyeglasses.class.getSimpleName());
            eyeglasses.setDesc("眼镜");
            eyeglasses.setLogistic(Logistic.OR);
            List<AttributeValue> eyeglassesValueList = new ArrayList<>();
            for (Eyeglasses eye : Eyeglasses.values()) {
                AttributeValue eyeglassesValue = new AttributeValue();
                eyeglassesValue.setValue(eye.ordinal());
                eyeglassesValue.setDesc(Eyeglasses.getDesc(eye));
                eyeglassesValueList.add(eyeglassesValue);
            }
            eyeglasses.setValues(eyeglassesValueList);
            attributeList.add(eyeglasses);

        } else if (type == SearchType.CAR) {
            return new ArrayList<>();
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getAttribute SearchType is error.");
        }
        return attributeList;
    }

    /**
     * 抓拍统计查询接口（马燊偲）
     * 查询指定时间段内，指定设备抓拍的图片数量、该设备最后一次抓拍时间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ipcId     设备ID
     * @return CaptureCount 查询结果对象。对象内封装了：该时间段内该设备抓拍张数，该时间段内该设备最后一次抓拍时间。
     */
    public CaptureCount captureCountQuery(String startTime, String endTime, String ipcId) {

        //CaptureCount是一个封装类，用于封装返回的结果。
        CaptureCount result = new CaptureCount();

        // 最终封装成的boolQueryBuilder 对象。
        BoolQueryBuilder totalBQ = QueryBuilders.boolQuery();
        //totalBQ = ;

        // 设备ID 的的boolQueryBuilder
        BoolQueryBuilder ipcIdBQ = QueryBuilders.boolQuery();
        //设备ID存在时的查询条件
        if (ipcId != null && !ipcId.equals("")) {
            ipcIdBQ.must(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcId)); //暂支持只传一个设备
            totalBQ.must(ipcIdBQ);
        }

        // 开始时间 的boolQueryBuilder
        BoolQueryBuilder startTimeBQ = QueryBuilders.boolQuery();
        //开始时间 存在时的查询条件
        if (null != startTime && !startTime.equals("")) {
            startTimeBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gte(startTime));//gte: >= 大于或等于
            totalBQ.must(startTimeBQ);
        }

        // 结束时间 的boolQueryBuilder
        BoolQueryBuilder endTimeBQ = QueryBuilders.boolQuery();
        // 结束时间 存在时的查询条件
        if (null != endTime && !endTime.equals("")) {
            startTimeBQ.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).lte(endTime)); //lte: <= 小于或等于
            totalBQ.must(endTimeBQ);
        }

        //所有判断结束后
        SearchResponse searchResponse = ElasticSearchHelper.getEsClient() //启动Es Java客户端
                .prepareSearch(DynamicTable.DYNAMIC_INDEX) //指定要查询的索引名称
                .setTypes(DynamicTable.PERSON_INDEX_TYPE) //指定要查询的类型名称
                .setQuery(totalBQ) //根据查询条件qb设置查询
                .addSort(DynamicTable.TIMESTAMP, SortOrder.DESC) //以时间字段降序排序
                .get();

        SearchHits hits = searchResponse.getHits(); //返回结果包含的文档放在数组hits中
        long totalresultcount = hits.getTotalHits(); //符合qb条件的结果数量

        //返回结果包含的文档放在数组hits中
        SearchHit[] searchHits = hits.hits();
        //若不存在符合条件的查询结果
        if (totalresultcount == 0) {
            LOG.error("The result count is 0! Last capture time does not exist!");
            result.setTotalresultcount(totalresultcount);
            result.setLastcapturetime("None");
        } else {
            /*
              获取该时间段内设备最后一次抓拍时间：
              返回结果包含的文档放在数组hits中，由于结果按照降序排列，
              因此hits数组里的第一个值代表了该设备最后一次抓拍的具体信息
              例如{"s":"XXXX","t":"2017-09-20 15:55:06","sj":"1555"}
              将该信息以Map形式读取，再获取到key="t“的值，即最后一次抓拍时间。
             */

            //获取最后一次抓拍时间
            String lastcapturetime = (String) searchHits[0].getSourceAsMap().get(DynamicTable.TIMESTAMP);

            /*
              返回值为：设备抓拍张数、设备最后一次抓拍时间。
             */
            result.setTotalresultcount(totalresultcount);
            result.setLastcapturetime(lastcapturetime);
        }
        return result;
    }

    /**
     * 查询抓拍历史记录（陈柯）
     * 根据条件筛选抓拍图片，并返回图片对象
     *
     * @param option option中包含count、时间段、时间戳、人脸属性等值，根据这些值去筛选
     *               符合条件的图片对象并返回
     * @return List<SearchResult>符合条件的图片对象以list形式返回
     */
    @Override
    public List<SearchResult> getCaptureHistory(SearchOption option) {
        CaptureHistory captureHistory = new CaptureHistory();
        List<String> ipcId = option.getDeviceIds();
        option.setSearchType(SearchType.PERSON);
        List<SortParam> sortParams = option.getSortParams();
        return captureHistory.getRowKey_history(option, ipcId,sortParams);
    }

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
    @Override
    public List<AttributeCount> captureAttributeQuery(String startTime, String endTime, List<String> ipcIdList, SearchType type) {
        List<AttributeCount> attributeCountList = new ArrayList<>();

        if (type == SearchType.PERSON) {
            CapturePictureSearchService service = new CapturePictureSearchServiceImpl();
            if (ipcIdList != null && ipcIdList.size() > 0) {
                for (String ipcId : ipcIdList) {
                    AttributeCount attributeCount = new AttributeCount();
                    attributeCount.setIPCId(ipcId);
                    CaptureCount captureCount = service.captureCountQuery(startTime, endTime, ipcId);
                    long count = captureCount.getTotalresultcount();
                    attributeCount.setCaptureCount(count);

                    List<Attribute> attributeList = service.getAttribute(type);
                    for (Attribute attribute : attributeList) {
                        List<AttributeValue> values = attribute.getValues();
                        for (AttributeValue attributeValue : values) {
                            BoolQueryBuilder FilterIpcId = QueryBuilders.boolQuery();
                            FilterIpcId.must(QueryBuilders.matchPhraseQuery(DynamicTable.IPCID, ipcId));
                            FilterIpcId.must(QueryBuilders.rangeQuery(DynamicTable.TIMESTAMP).gt(startTime).lt(endTime));
                            FilterIpcId.must(QueryBuilders.matchPhraseQuery(attribute.getIdentify().toLowerCase(), attributeValue.getValue()));
                            SearchResponse searchResponse = ElasticSearchHelper.getEsClient()
                                    .prepareSearch(DynamicTable.DYNAMIC_INDEX)
                                    .setTypes(DynamicTable.PERSON_INDEX_TYPE)
                                    .setQuery(FilterIpcId).get();
                            SearchHits hits = searchResponse.getHits();
                            long totalHits = hits.getTotalHits();
                            attributeValue.setCount(totalHits);
                        }
                    }
                    attributeCount.setAttributes(attributeList);
                    attributeCountList.add(attributeCount);
                }
            } else {
                LOG.error("ipcIdList is null.");
            }
        } else if (type == SearchType.CAR) {
            LOG.error("No vehicle queries are currently supported");
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.captureAttributeQuery SearchType is error");
        }
        return attributeCountList;
    }
}
