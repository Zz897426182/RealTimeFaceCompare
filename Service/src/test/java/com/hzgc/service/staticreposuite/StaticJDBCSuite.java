package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.jni.FaceFunction;
import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.service.staticrepo.PhoenixJDBCHelper;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class StaticJDBCSuite {
    private ObjectInfoHandlerImpl infoHandler = new ObjectInfoHandlerImpl();

    @Test
    public void testGetPhoenixConnection() {
        Connection conn = PhoenixJDBCHelper.getPhoenixJdbcConn();
        System.out.printf("connection testing right : " + conn);
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddObjectInfo() throws IOException {
        Map<String, Object> person  = new HashMap<>();
        byte[] phtoto = FaceFunction.inputPicture(ClassLoader.getSystemResource("2018_02_01_20_06_03_9288_1.jpg").getPath());
        System.out.println("photo" + phtoto.toString());
        FaceAttribute faceAttribute = FaceFunction.featureExtract(phtoto);
        float[] feature = faceAttribute.getFeature();
        Random random = new Random();
        String idCard = (random.nextInt(900000000) + 100000000) + ""
                + (random.nextInt(900000000) + 100000000);
        Map<String, Object> map = new HashMap<>();
        person.put(ObjectInfoTable.PKEY, "0042002");
        person.put(ObjectInfoTable.PLATFORMID, "0042");
        person.put(ObjectInfoTable.NAME, "nameValue");
        person.put(ObjectInfoTable.SEX, 1);
        person.put(ObjectInfoTable.IDCARD, idCard);
        person.put(ObjectInfoTable.PHOTO,  phtoto);
        person.put(ObjectInfoTable.FEATURE, feature);
        person.put(ObjectInfoTable.CREATOR, "creatorValue");
        person.put(ObjectInfoTable.CPHONE, "18069773749");
        person.put(ObjectInfoTable.CREATETIME, new  java.sql.Date(System.currentTimeMillis()));
        person.put(ObjectInfoTable.UPDATETIME, new  java.sql.Date(System.currentTimeMillis()));
        person.put(ObjectInfoTable.TAG, "1");
        person.put(ObjectInfoTable.IMPORTANT, 1);
        person.put(ObjectInfoTable.STATUS, 0);
        person.put(ObjectInfoTable.REASON,  "reasonValue");
        infoHandler.addObjectInfo("0042", person);
    }

    @Test
    public void testDeleteObjectInfo() {
        List<String> rowkeys = new ArrayList<>();
        rowkeys.add("001bb6ff24c943f8a90a833258c03250");
        infoHandler.deleteObjectInfo(rowkeys);
    }

    @Test
    public void testUpdateObjectInfo() {
        Map<String, Object> person = new HashMap<>();
        person.put(ObjectInfoTable.ROWKEY, "00834612140f491990efa13d573bca4d");
        person.put(ObjectInfoTable.NAME, "wangnimaenha");
        person.put(ObjectInfoTable.PLATFORMID, "0042");
        infoHandler.updateObjectInfo(person);
    }
}
