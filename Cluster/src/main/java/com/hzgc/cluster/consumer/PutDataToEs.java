package com.hzgc.cluster.consumer;


import com.hzgc.dubbo.Attribute.*;
import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.dynamicrepo.DynamicTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.jni.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import org.elasticsearch.action.index.IndexResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class PutDataToEs implements Serializable{
    public void putDataToEs(String ftpurl, FaceObject faceObject) {
        String timestamp = faceObject.getTimeStamp();
        String ipcid = faceObject.getIpcId();
        String timequantum = faceObject.getTimeSlot();
        int picType = faceObject.getType().getType();
        FaceAttribute attributes = faceObject.getAttribute();
        Map<String, Object> map = new HashMap<>();
        Map<String,Object> faceattr = new HashMap<>();
        FaceAttribute faceAttr =  faceObject.getAttribute();
        float[] feature = faceAttr.getFeature();
        String newfeature = FaceFunction.floatArray2string(feature);
        faceattr.put("feature",newfeature);
        HairColor hairColor = faceAttr.getHairColor();
        int haircolor = hairColor.getHaircolor();
        faceattr.put("haircolor",haircolor);
        Eyeglasses eyeGlasses = faceAttr.getEyeglasses();
        int eleglasses = eyeGlasses.getEyeglassesvalue();
        faceattr.put("eleglasses",eleglasses);
        Gender Gender = faceAttr.getGender();
        int gender = Gender.getGendervalue();
        faceattr.put("gender", gender);
        HairStyle hairStyle = faceAttr.getHairStyle();
        int hairstyle = hairStyle.getHairstyle();
        faceattr.put("hairstyle", hairstyle);
        Hat Hat = faceAttr.getHat();
        int hat = Hat.getHatvalue();
        faceattr.put("hat",hat);
        Huzi Huzi = faceAttr.getHuzi();
        int huzi = Huzi.getHuzivalue();
        faceattr.put("huzi",huzi);
        Tie Tie = faceAttr.getTie();
        int tie = Tie.getTievalue();
        faceattr.put("tie",tie);
        map.put("picType",picType);
        map.put("url",ftpurl);
        map.put("timestamp", timestamp);
        map.put("ipcid", ipcid);
        map.put("timequantum", timequantum);
        map.put("attributes",faceattr);
        if (ftpurl != null) {
            String path = ftpurl.substring(ftpurl.lastIndexOf(":"));
            String path1 = path.substring(path.indexOf("/"));
            String rowkey = FtpUtil.transformNameToKey(path1);
            IndexResponse indexResponse = ElasticSearchHelper.getEsClient().prepareIndex(DynamicTable.DYNAMIC_INDEX,
                    DynamicTable.PERSON_INDEX_TYPE, rowkey).setSource(map).get();
        }
    }
}
