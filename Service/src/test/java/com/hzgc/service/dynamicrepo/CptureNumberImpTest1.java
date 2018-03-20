package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CptureNumberImpTest1 {
    public static void main(String[] args) {
        SearchOption searchOption = new SearchOption();
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add("3B0383FPAG00883");
        deviceIds.add("3K01E84PAU00150");
        deviceIds.add("2L04129PAU01933");
        searchOption.setDeviceIds(deviceIds);
        List<SortParam> sortParams = new ArrayList<>();
        sortParams.add(SortParam.IPC);
        sortParams.add(SortParam.TIMEDESC);
        searchOption.setSortParams(sortParams);
        searchOption.setOffset(0);
        searchOption.setCount(1000000000);
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        List<SearchResult> list = capturePictureSearchService.getCaptureHistory(searchOption);
        for (SearchResult s : list){
            List<SingleResult> singlelist = s.getResults();
            for (SingleResult ss : singlelist){
                List<CapturedPicture> capturedPictures = ss.getPictures();
                System.out.println(capturedPictures.size());
            }
        }
        System.out.println(list.size());
    }
}
