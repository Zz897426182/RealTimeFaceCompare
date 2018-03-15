package com.hzgc.service.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.jni.FaceFunction;
import com.hzgc.service.staticrepo.ObjectInfoHandlerImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PrepareDataInStatic {
    private static ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
    public static void main(String[] args) {
        String photosPath = "/opt/smallpics.bak";
        File[] files = getFileList(photosPath);
        for (int i = 0;i < files.length; i++) {
            String path = files[i].getAbsolutePath();
            byte[] photo = FaceFunction.inputPicture(path);
            float[] feature = FaceFunction.featureExtract(path).getFeature();

            Map<String, Object> person  = new HashMap<>();
            Random random = new Random();
            String idCard = (random.nextInt(900000000) + 100000000) + ""
                    + (random.nextInt(900000000) + 100000000);
            if (i % 10 == 0) {
                person.put(ObjectInfoTable.PLATFORMID, "0042");
                person.put(ObjectInfoTable.PKEY, "0042002");
                person.put(ObjectInfoTable.ROWKEY, "0042002" + idCard);
            } else if (i %3 == 0) {
                person.put(ObjectInfoTable.PLATFORMID, "0041");
                person.put(ObjectInfoTable.PKEY, "0041001");
                person.put(ObjectInfoTable.ROWKEY, "0041001" + idCard);
            } else {
                person.put(ObjectInfoTable.PLATFORMID, "0040");
                person.put(ObjectInfoTable.PKEY, "0040002");
                person.put(ObjectInfoTable.ROWKEY, "0040002" + idCard);
            }
            person.put(ObjectInfoTable.NAME, "nameValue" + i);
            person.put(ObjectInfoTable.SEX, 1);
            if(photo != null && feature != null) {
                person.put(ObjectInfoTable.PHOTO,  photo);
                person.put(ObjectInfoTable.FEATURE, feature);
            }
            person.put(ObjectInfoTable.IDCARD, idCard);
            person.put(ObjectInfoTable.CREATOR, "creatorValue" + i );
            person.put(ObjectInfoTable.CPHONE, "18069773749" + i);
            person.put(ObjectInfoTable.CREATETIME, new  java.sql.Date(System.currentTimeMillis()));
            person.put(ObjectInfoTable.UPDATETIME, new  java.sql.Date(System.currentTimeMillis()));
            person.put(ObjectInfoTable.TAG, "1");
            person.put(ObjectInfoTable.IMPORTANT, 1);
            person.put(ObjectInfoTable.STATUS, 0);
            person.put(ObjectInfoTable.REASON,  "reasonValue");
            objectInfoHandler.addObjectInfo("0042", person);
            System.out.println("now in insert the " + (i + 1)+ "tiao data....");
        }
    }

    public static File[] getFileList(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            System.out.printf("please input the pics's parent path...");
            System.exit(0);
        }
        return file.listFiles();
    }
}
