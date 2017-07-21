package com.hzgc.hbase.staticreposuite;

import com.hzgc.hbase.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.hbase.staticrepo.StaticRepoUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class StaticRepoUtilSuite {

    @Test
    public void testgetIdCardByRandom(){
        String idcard = StaticRepoUtil.getIdCardByRandom();
        System.out.println("idcard: " + idcard + ",length:" + idcard.length());
    }

    @Test
    public void testAddObjectInfo(){
        String platformId = "1234";
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("name", "小王炸");
        person.put("idcard", "111111111111111111");
        person.put("sex", "1");
        try {
            person.put("photo", Image2Byte2Image.image2byte("C:\\Users\\lenovo\\Desktop\\temp\\nima.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        person.put("reason", "赌博");
        person.put("pkey", "123456");
        person.put("creator", "羊驼");
        person.put("cphone", "88888888888");
        person.put("tag", "person");

        int flag = new ObjectInfoHandlerImpl().addObjectInfo(platformId, person);
        System.out.println(flag);
    }
    @Test
    public void testUpdateObjectInfo(){
        String platformId = "1234";
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("rowkey", "111111111111111111123456");
        person.put("name", "小王炸炸");
        person.put("idcard", "222111111111111111");
        person.put("sex", "0");
        try {
            person.put("photo", Image2Byte2Image.image2byte("C:\\Users\\lenovo\\Desktop\\temp\\nima.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        person.put("reason", "赌博+暴力倾向");
        person.put("creator", "羊驼神兽");

        int flag = new ObjectInfoHandlerImpl().updateObjectInfo(person);
        System.out.println(flag);
    }

    @Test
    public void testDeleteObjectInfo(){
        List<String> rowkeys = new ArrayList<String>();
        rowkeys.add("111111111111111111123456");
        int flag = new ObjectInfoHandlerImpl().deleteObjectInfo(rowkeys);
        System.out.println(flag);
    }
}
