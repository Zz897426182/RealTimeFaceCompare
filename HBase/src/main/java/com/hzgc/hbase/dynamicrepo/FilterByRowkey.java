package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FilterByRowkey {
    private static Logger LOG = Logger.getLogger(FilterByRowkey.class);

    public SearchRequestBuilder getSearchRequestBuilder(SearchOption option){
        SearchType searchType = option.getSearchType();
        List<String> deviceId = option.getDeviceIds();
        Iterator it = deviceId.iterator();
        Date startTime = option.getStartDate();
        Date endTime = option.getEndDate();
        String index ="dynamic";
        String type = "person";
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
        if(searchType.equals(searchType.PERSON)){
            if (deviceId != null){
                while (it.hasNext()){
                    String t = (String) it.next();
                    boolQueryBuilder1.should(QueryBuilders.matchPhraseQuery("f", t).analyzer("standard"));
                }
                boolQueryBuilder.must(boolQueryBuilder1);
            }
            if (startTime != null && endTime != null){
                String start = simpleDateFormat.format(startTime);
                String end = simpleDateFormat.format(endTime);
                boolQueryBuilder.must(QueryBuilders.rangeQuery("t").gt(start).lt(end));
            }
        }else {

        }
        return ElasticSearchHelper.getEsClient().prepareSearch(index)
                .setTypes(type).setExplain(true).setQuery(boolQueryBuilder);
    }
    public List<String> getSearchResponse(SearchRequestBuilder searchRequestBuilder){
        SearchResponse searchResponse = searchRequestBuilder.get();
        SearchHits hits = searchResponse.getHits();
        List<String> RowKey = new ArrayList<>();
        SearchHit[] hits1 = hits.getHits();
        if (hits1.length > 0 ) {
            for (SearchHit hit : hits1) {
                String rowKey = hit.getId();
                RowKey.add(rowKey);
            }
        }
        return RowKey;
    }
    /**
     *
     * @param option 搜索选项
     * @return List<String> 符合条件的rowKey集合
     */
    public List<String> getRowKey(SearchOption option){
        SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(option);
        return getSearchResponse(searchRequestBuilder);
    }

    /**
     * 根据车牌号过滤rowKey范围
     *
     * @param option 搜索选项
     * @param scan   scan对象
     * @return List<String> 符合条件的rowKey集合
     */
    public List<String> filterByPlateNumber(SearchOption option, Scan scan) {
        List<String> rowKeyList = new ArrayList<>();

        if (option.getPlateNumber() == null) {
            String plateNumber = option.getPlateNumber();
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                ResultScanner scanner = car.getScanner(scan);
                Map<String, String> map = new HashMap<>();
                for (Result result : scanner) {
                    byte[] rowKey = result.getRow();
                    String rowKeyStr = Bytes.toString(rowKey);
                    byte[] plateNum = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM);
                    String plateNumStr = Bytes.toString(plateNum);
                    if (rowKey != null && rowKey.length > 0 && plateNumStr != null && plateNumStr.length() > 0) {
                        map.put(rowKeyStr, plateNumStr);
                    }
                }
                if (!map.isEmpty()) {
                    Iterator<String> iter = map.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        String value = map.get(key);
                        if (value.contains(plateNumber)) {
                            rowKeyList.add(key);
                        }
                    }
                } else {
                    LOG.info("map is empty,used method FilterByRowkey.filterByPlateNumber.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(car);
            }
        } else {
            LOG.error("param is empty,used method FilterByRowkey.filterByPlateNumber.");
        }
        return rowKeyList;
    }

    public List<String> filterByDate(List<String> rowKeyList, String startDate, String endDate, Scan scan, Table table) {
        int start = Integer.parseInt(startDate);
        int end = Integer.parseInt(endDate);

        List<Filter> filterList = new ArrayList<>();
        Filter startFilter = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new RegexStringComparator(".*" + start + ".*"));
        filterList.add(startFilter);
        Filter endFilter = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, new RegexStringComparator(".*" + end + "_" + ".*"));
        filterList.add(endFilter);
        FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL, filterList);

        scan.setFilter(filter);
        try {
            ResultScanner scanner = table.getScanner(scan);
            for (Result result : scanner) {
                byte[] bytes = result.getRow();
                String string = Bytes.toString(bytes);
                rowKeyList.add(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("filter rowkey by Date failed! used method FilterByRowkey.filterByDate.");
        } finally {
            HBaseUtil.closTable(table);
        }
        return rowKeyList;
    }
}
