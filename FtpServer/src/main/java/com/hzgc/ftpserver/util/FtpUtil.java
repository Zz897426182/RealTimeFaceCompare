package com.hzgc.ftpserver.util;

import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FtpUtil implements Serializable {
    private static Logger LOG = Logger.getLogger(FtpUtil.class);

    public static boolean checkPort(int checkPort) throws Exception {
        return checkPort > 1024;
    }

    public static ByteArrayOutputStream inputStreamCacher(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            IoUtils.close(baos);
            IoUtils.close(is);
        }
        return baos;
    }

    /**
     * @param pictureName determine the picture type based on the file name
     * @return equals 0, it is a picture
     * lager than 0, it is a face picture
     */
    public static int pickPicture(String pictureName) {
        int picType = 0;
        if (null != pictureName) {
            String tmpStr = pictureName.substring(pictureName.lastIndexOf("_") + 1, pictureName.lastIndexOf("."));
            try {
                picType = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return picType;
    }

    public static String faceKey(int faceNum, String key) {
        StringBuilder faceKey = new StringBuilder();
        if (faceNum < 10) {
            key = key.substring(0, key.lastIndexOf("_"));
            faceKey.append(key).append("_0").append(faceNum);
        } else if (faceNum >= 10 && faceNum < 100) {
            key = key.substring(0, key.lastIndexOf("_"));
            faceKey.append(key).append("_").append(faceNum);
        } else {
            faceKey.append(key);
        }
        return faceKey.toString();
    }

    public static String transformNameToKey(String fileName) {
        StringBuilder key = new StringBuilder();
        String newFileName = "";
        if (fileName != null && fileName.length() > 0) {
            newFileName = fileName.substring(11, fileName.length());
            String ipcID = newFileName.substring(1, newFileName.indexOf("/", 2));
            String tempKey = newFileName.substring(newFileName.lastIndexOf("/"), newFileName.lastIndexOf("_")).replace("/", "");
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(2, tempKey.lastIndexOf("_")).replace("_", "");

            StringBuffer prefixNameKey = new StringBuffer();
            prefixNameKey = prefixNameKey.append(prefixName).reverse();
            if (prefixName.length() < 10) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 10 - prefixName.length(); i++) {
                    stringBuilder.insert(0, "0");
                }
                prefixNameKey.insert(0, stringBuilder);
            }
            key.append(ipcID).append("_").append(timeName).append("_").append(prefixNameKey).append("_00");
        } else {
            key.append(newFileName);
        }
        return key.toString();
    }

    public static Map<String, String> getRowKeyMessage(String rowKey) {
        String ipcID = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.lastIndexOf("_"));

        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        String second = timeStr.substring(10, 12);

        StringBuilder time = new StringBuilder();
        time = time.append(20).append(year).append("-").append(month).append("-").append(day).append(" ").append(hour).append(":").append(minute).append(":").append(second);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder sj = new StringBuilder();
        sj = sj.append(hour).append(minute);

        Map<String, String> map = new HashMap<>();
        try {
            Date date = sdf.parse(time.toString());
            long timeStamp = date.getTime();
            map.put("ipcID", ipcID);
            map.put("time", String.valueOf(timeStamp));
            map.put("mqkey", time.toString());
            map.put("sj", sj.toString());//sj为动态库同步ES所需字段
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
