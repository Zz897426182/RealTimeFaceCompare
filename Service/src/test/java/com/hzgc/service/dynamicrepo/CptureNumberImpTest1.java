package com.hzgc.service.dynamicrepo;

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
        List<String> ipcid = new ArrayList<>();
        ipcid.add("DS-2DE72XYZIW-ABCVS20160823CCCH641752612");
        ipcid.add("3K01E84PAU00483");
        ipcid.add("DS-2CD2T20FD-I320160122AACH571485690");
        ipcid.add("3B0383FPAG00883");
        ipcid.add("offlineWarnRowKey");
        ipcid.add("3K01E84PAU00150");
        ipcid.add("2B007C3PAW00002");
        ipcid.add("2L04129PAU01933");
        ipcid.add("3K01E84PAU00083");
        ipcid.add("3K01E84PAU00498");
        String startTime = "2018-03-10 08:00:00";
        String endTime = "2018-03-10 11:00:00";
        CptureNumberImpl cptureNumber = new CptureNumberImpl();
        Map<String,Integer> map = cptureNumber.timeSoltNumber(ipcid,startTime,endTime);
        System.out.println(map);
//        System.out.println(Integer.MAX_VALUE);
//        CptureNumberImpl cptureNumber = new CptureNumberImpl();
//        Map<String,Integer> map = cptureNumber.dynaicNumberService(lists);
//        Map<String,Integer> map1 = cptureNumber.staticNumberService("0001");
//        System.out.println(map1);
    }
}
