package com.hzgc.service.dynamicrepo;

import com.hzgc.dubbo.attribute.Attribute;
import com.hzgc.dubbo.attribute.AttributeValue;
import com.hzgc.dubbo.attribute.Logistic;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.dynamicrepo.TimeInterval;

import java.util.ArrayList;
import java.util.List;

public class SQLTest {
    public static void main(String[] args) {
        SearchOption option = new SearchOption();
        option.setSearchType(SearchType.PERSON);
        option.setThreshold(60f);
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add("aaa_IPCID");
        deviceIds.add("bbb_IPCID");
        option.setDeviceIds(deviceIds);
        option.setStartDate("2018-01-03 00:00:00");
        option.setEndDate("2018-01-03 24:00:00");
        List<TimeInterval> timeIntervalList = new ArrayList<>();
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setStart(60);
        timeInterval.setEnd(60);
        timeIntervalList.add(timeInterval);
        option.setIntervals(timeIntervalList);
        List<Attribute> attributeList = new ArrayList<>();
        Attribute attribute = new Attribute();
        attribute.setIdentify("gender");
        attribute.setLogistic(Logistic.AND);
        List<AttributeValue> attributeValueList = new ArrayList<>();
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setValue(1);
        attributeValueList.add(attributeValue);
        attribute.setValues(attributeValueList);
        attributeList.add(attribute);
        option.setAttributes(attributeList);
        //option.setSortParams("similarity");


        String SQL = ParseByOption.getFinalSQLwithOption("",option);
        System.out.println(SQL);
    }

}


