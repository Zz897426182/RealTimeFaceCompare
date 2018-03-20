package com.hzgc.service.dynamicrepo;


import com.hzgc.dubbo.dynamicrepo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CptureNumberImpTest1 {
    public static void main(String[] args) {
        SearchOption searchOption = new SearchOption();
        List<String> deviceIds = new ArrayList<>();
        //deviceIds.add("3B0383FPAG00883");
        //deviceIds.add("3K01E84PAU00150");
        //deviceIds.add("2L04129PAU01933");
        //deviceIds.add("DS-2CD2T20FD-I320160122AACH571485690");
        searchOption.setDeviceIds(deviceIds);
        List<SortParam> sortParams = new ArrayList<>();
       // sortParams.add(SortParam.IPC);
        sortParams.add(SortParam.TIMEDESC);
        searchOption.setSortParams(sortParams);
        searchOption.setStartDate("2018-03-12 00:00:00");
        searchOption.setEndDate("2018-03-20 18:00:00");
        searchOption.setOffset(0);
        searchOption.setCount(30);
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        List<SearchResult> list = capturePictureSearchService.getCaptureHistory(searchOption);
        for (SearchResult s : list) {
            List<SingleResult> singlelist = s.getResults();
            for (SingleResult ss : singlelist) {
                List<CapturedPicture> capturedPictures = ss.getPictures();
                for (CapturedPicture c : capturedPictures) {
                    String a = c.getSurl();
                    System.out.println(a);
                }
            }
        }
    }
}
