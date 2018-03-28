package com.hzgc.service.device;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchResult;
import com.hzgc.service.dynamicrepo.CapturePictureSearchServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-11-4.
 */
public class CapturePictureCatchHistoryTest {
    public static void main(String[] args) {
//        String ftpurl = "ftp://192.168.1.28:2121/3B0383FPAG00883/2017/05/23/16/00/2017_05_23_16_00_15_5704_0.jpg";
//        int haircolor = 1;
//        int eleglasses = 1;
//        int gender = 1;
//        int hairstyle = 1;
//        int hat = 1;
//        int huzi = 1;
//        int tie = 1;
//        String date = "2017-1-10";
//        String pictype = "PERSON";
//        String timestamp = "2017-11-04 11:17:56";
//        String ipcid = "001";
//        String timeslot = "1117";
//        Map<String, Object> map = new HashMap<>();
//        map.put("haircolor", haircolor);
//        map.put("eleglasses", eleglasses);
//        map.put("gender", gender);
//        map.put("hairstyle", hairstyle);
//        map.put("hat", hat);
//        map.put("huzi", huzi);
//        map.put("tie", tie);
//        map.put("date", date);
//        map.put("pictype", pictype);
//        map.put("timestamp", timestamp);
//        map.put("ipcid", ipcid);
//        map.put("timeslot", timeslot);
//        IndexResponse indexResponse = ElasticSearchHelper.getEsClient()
//                .prepareIndex(DynamicTable.DYNAMIC_INDEX, DynamicTable.PERSON_INDEX_TYPE, ftpurl)
//                .setSource(map).get();
//        System.out.println(indexResponse.getVersion());
        SearchOption searchOption = new SearchOption();
        Integer ab = 20;
        Integer ac = 0;
        searchOption.setCount(ab);
        searchOption.setOffset(ac);
        List<String> a = new ArrayList<>();
        String b = "2L04129PAU01933";
        String c = "DS-2DE72XYZIW-ABCVS20160823CCCH641752612";
        String d = "3B0383FPAG00883";
        a.add(b);
        a.add(c);
        a.add(d);
        searchOption.setDeviceIds(a);
        searchOption.setSortParams("-timestamp");
        searchOption.setThreshold(0.0f);
        searchOption.setSearchType(null);
        searchOption.setImage(null);
        searchOption.setImageId(null);
        searchOption.setEndDate(null);
        searchOption.setStartDate(null);
        searchOption.setPlatformId(null);
        searchOption.setPlateNumber(null);
        searchOption.setIntervals(null);
        searchOption.setAttributes(null);
//        List<Attribute> attributes = new ArrayList<>();
//        Attribute attribute = new Attribute();
//        attribute.setIdentify("haircolor");
//        attribute.setLogistic(Logistic.AND);
//        List<AttributeValue> attributeValues = new ArrayList<>();
//        AttributeValue attributeValue = new AttributeValue();
//        attributeValue.setValue(1);
//        attributeValue.setDesc("黄色");
//        AttributeValue attributeValue1 = new AttributeValue();
//        attributeValue1.setValue(2);
//        attributeValue1.setDesc("红色");
//        attributeValues.add(attributeValue1);
//        attributeValues.add(attributeValue);
//        attribute.setValues(attributeValues);
//        attributes.add(attribute);
        //searchOption.setAttributes(null);
        CapturePictureSearchServiceImpl capturePictureSearchService = new CapturePictureSearchServiceImpl();
        SearchResult searchResult = capturePictureSearchService.getCaptureHistory(searchOption);
        System.out.println(searchResult);
    }
}
