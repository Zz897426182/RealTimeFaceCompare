package com.hzgc.cluster.consumer;

import com.hzgc.collect.expand.processer.FaceObject;
import com.hzgc.service.dynamicrepo.DynamicTable;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.dubbo.feature.FaceAttribute;
import org.elasticsearch.action.index.IndexResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PutDataToEs implements Serializable {
    private static PutDataToEs instance = null;

    public static PutDataToEs getInstance() {
        if (instance == null) {
            synchronized (PutDataToEs.class) {
                if (instance == null) {
                    instance = new PutDataToEs();
                }
            }
        }
        return instance;
    }

    public int putDataToEs(String ftpurl, FaceObject faceObject) {
        String timestamp = faceObject.getTimeStamp();
        String ipcid = faceObject.getIpcId();
        String timeslot = faceObject.getTimeSlot();
        String date = faceObject.getDate();
        IndexResponse indexResponse = new IndexResponse();
        String searchtype = faceObject.getType().name();
        Map<String, Object> map = new HashMap<>();
        FaceAttribute faceAttr = faceObject.getAttribute();
        int haircolor = faceAttr.getHairColor();
        map.put(DynamicTable.HAIRCOLOR, haircolor);
        int eleglasses = faceAttr.getEyeglasses();
        map.put(DynamicTable.ELEGLASSES, eleglasses);
        int gender = faceAttr.getGender();
        map.put(DynamicTable.GENDER, gender);
        int hairstyle = faceAttr.getHairStyle();
        map.put(DynamicTable.HAIRSTYLE, hairstyle);
        int hat = faceAttr.getHat();
        map.put(DynamicTable.HAT, hat);
        int huzi = faceAttr.getHuzi();
        map.put(DynamicTable.HUZI, huzi);
        int tie = faceAttr.getTie();
        map.put(DynamicTable.TIE, tie);
        int sharpness = faceAttr.getSharpness();
        map.put(DynamicTable.SHARPNESS, sharpness);
        map.put(DynamicTable.DATE, date);
        map.put(DynamicTable.SEARCHTYPE, searchtype);
        map.put(DynamicTable.TIMESTAMP, timestamp);
        map.put(DynamicTable.IPCID, ipcid);
        map.put(DynamicTable.TIMESLOT, timeslot);
        if (ftpurl != null) {
            indexResponse = ElasticSearchHelper.getEsClient().prepareIndex(DynamicTable.DYNAMIC_INDEX,
                    DynamicTable.PERSON_INDEX_TYPE, ftpurl).setSource(map).get();
        }
        if (indexResponse.getVersion() == 1) {
            return 1;
        } else {
            return 0;
        }
    }
}
