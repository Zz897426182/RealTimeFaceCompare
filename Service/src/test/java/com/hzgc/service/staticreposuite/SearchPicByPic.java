package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.service.staticrepo.ElasticSearchHelper;
import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.util.HBaseHelper;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchPicByPic {
    private ObjectInfoHandlerImpl objectInfoHandler;
    @Before
    public void init(){
        objectInfoHandler = new ObjectInfoHandlerImpl();
    }

    @Test
    public void addDataToPerson(){
        File root = new File("/opt/ldl/small_pic");
        File[] files = root.listFiles();
        String platformId = "1234";
        Map<String, Object> person = new HashMap<String, Object>();

        person.put("tag", "person");
        for (int i = 0; i < files.length; i++){
            if (i % 2 == 0){
                person.put("name", "花满天");
                person.put("creator", "羊驼");
                person.put("pkey", "123456");
                person.put("reason", "赌博");
                platformId = "12345";
            } else if (i % 3 == 0){
                person.put("name", "花满天1");
                person.put("creator", "羊驼1");
                person.put("pkey", "123457");
                person.put("reason", "赌博1");
                platformId = "12348";
            } else if (i % 5 == 0){
                person.put("name", "1花满天2");
                person.put("creator", "2羊驼");
                person.put("pkey", "123458");
                person.put("reason", "a赌博");
                platformId = "12343";
            } else {
                person.put("name", "花1满天2");
                person.put("creator", "2羊驼");
                person.put("pkey", "223458");
                person.put("reason", "ada赌博dda");
                platformId = "12341";
            }
            if (new Random().nextInt(5000) % 2  == 0){
                person.put("sex", "1");
            } else {
                person.put("sex", "2");
            }
            person.put("cphone", (new Random().nextInt(1000000000) + 1000000000) + ""
                    + (new Random().nextInt(1000000000) + 1000000000));
            person.put("idcard", (new Random().nextInt(1000000000) + 1000000000) + ""
                    + (new Random().nextInt(1000000000) + 1000000000));
            byte[] photo = null;
            try {
                photo = Image2Byte2Image.image2byte(files[i].getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            person.put("photo", photo);
            String feature = objectInfoHandler.getFeature("person", photo);
            if (feature != null && !"".equals(feature)){
                person.put("feature", feature);
            }
            int flag = objectInfoHandler.addObjectInfo(platformId, person);
            System.out.println("flag" + flag + "======================== " + i + ": " +feature);
            System.out.println(flag);
        }

        int flag = new ObjectInfoHandlerImpl().addObjectInfo(platformId, person);
        System.out.println(flag);
    }

    @Test
    public  void  testInsertDataIntoHBase(){
        File root = new File("/opt/ldl/small_pic");
        File[] files = root.listFiles();
        byte[] photo = null;
        int fileNums = files.length;
        Table table = HBaseHelper.getTable("person");
        Put put = null;
        List<Put> puts = new ArrayList<>();
        for (int i = 0;i < files.length; i++){
            put = new Put(Bytes.toBytes(UUID.randomUUID().toString().replace("-", "")));
            if (i % 2 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0002"));
            } else if (i % 3 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0003"));
            } else if (i % 5 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0004"));
            } else if (i % 7 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0005"));
            } else if (i % 11 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0006"));
            } else if (i % 13 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0007"));
            } else if (i % 17 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0008"));
            } else if (i % 19 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0009"));
            } else if (i % 23 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0010"));
            } else if (i % 29 == 0){
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0011"));
            } else {
                put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("f"), Bytes.toBytes("17130NCY0HZ0012"));
            }
            try {
                photo = Image2Byte2Image.image2byte(files[i].getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String feature = objectInfoHandler.getFeature("person", photo);
            put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("fea"), Bytes.toBytes(feature));
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = format.format(date);
            put.addColumn(Bytes.toBytes("i"), Bytes.toBytes("t"), Bytes.toBytes(dateString));
            puts.add(put);
            System.out.println(i + "====================,feature  length: " + feature.length() + ", text is " + feature);
            if (i % 5 == 0){
                try {
                    table.put(puts);
                    puts.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(i + "=================, puts size: " + puts.size());
            }
            if (puts.size() > 0){
                try {
                    table.put(puts);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        table.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Test
    public void testSearchPicByPic() throws IOException {
        String filePath = "/opt/ldl/small_pic/2017_04_24_06_33_16_28366_1.jpg";
        float[] feature_curr = FaceFunction.featureExtract(filePath).getFeature();
        String feature_curr_s = FaceFunction.floatArray2string(feature_curr);
        byte [] photo = Image2Byte2Image.image2byte(filePath);
        ObjectSearchResult objectSearchResult =objectInfoHandler.searchByPhotoAndThreshold("12341", photo,
                60, feature_curr_s, 1, 100);
        System.out.println(objectSearchResult.getSearchNums() +  "  "  + objectSearchResult.getSearchId()  + " "
                + objectSearchResult.getPhotoId()  + " " + objectSearchResult.getResults().size());

    }

    @Test
    public void testVoid(){
        Client client = ElasticSearchHelper.getEsClient();
        GetResponse response = client.prepareGet("objectinfo",
                "person",
                "1e2cf4008e3d4e749eeb92fd11e09c0b")
                .get();
        String feature_his = (String) response.getSource().get("feature");
        String filePath = "/opt/ldl/small_pic/2017_04_24_06_33_16_28366_1.jpg";
        float[] feature_curr = FaceFunction.featureExtract(filePath).getFeature();
        System.out.println(NativeFunction.compare(feature_curr, FaceFunction.string2floatArray(feature_his)));
    }
}
