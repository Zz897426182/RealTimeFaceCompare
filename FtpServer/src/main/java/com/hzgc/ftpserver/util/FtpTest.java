package com.hzgc.ftpserver.util;


import org.apache.log4j.Logger;

import java.io.*;

public class FtpTest {
    private static Logger LOG = Logger.getLogger(FtpTest.class);

    private static final String rowKey = "3B0383FPAG00883_170930093056_0000004174_00";
    private static final String fileName = "/3B0383FPAG00883/2017/09/30/0000000000000000-0/09/30/2017_09_30_09_30_56_27720_0.json";

    public static void main(String[] args) {
        InputStream is = data2inputStream();
        String jsonFileName = key2jsonFileName(rowKey);
        String pictureFileName = key2pictureFileName(rowKey);
        dataSaveIocal(is, rowKey, jsonFileName);
        dataSaveIocal(is, rowKey, pictureFileName);
    }

    public static InputStream data2inputStream() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("E:\\photo\\20170527113139.jpg");
            int len = fis.read();
            while (len != -1) {
                len = fis.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fis;
    }


    /**
     * 保存数据到本地
     *
     * @param is     数据流
     * @param rowKey rowKey
     */
    public static void dataSaveIocal(InputStream is, String rowKey, String fileName) {
        OutputStream os = null;
        byte[] bs = new byte[1024];
        String path = key2filePath(rowKey);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            os = new FileOutputStream(file.getPath() + File.separator + fileName);
            int len = is.read(bs);
            while (len != -1) {
                os.write(bs, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String key2pictureFileName(String rowKey) {
        StringBuilder pictureFileName = new StringBuilder();
        pictureFileName.append(rowKey).append(".jpg");
        return pictureFileName.toString();
    }

    /**
     * 通过rowKey解析到json文件名称
     *
     * @param rowKey rowKey
     * @return jsonFileName 文件名称
     */
    public static String key2jsonFileName(String rowKey) {
        StringBuilder jsonFileName = new StringBuilder();

        int type = Integer.parseInt(rowKey.substring(rowKey.lastIndexOf("_") + 1, rowKey.length()));
        if (type == 0) {
            jsonFileName = jsonFileName.append(rowKey).append(".json");
        } else if (type > 0) {
            //StringBuilder key = new StringBuilder();
            //key = key.append(rowKey.substring(0, rowKey.lastIndexOf("_"))).append("_00");
            rowKey = rowKey.substring(0, rowKey.lastIndexOf("_") + 1) + "00";
            jsonFileName = jsonFileName.append(rowKey).append(".json");
            //TODO:"/opt/ftpserver/"将从配置文件读取
        } else {
            LOG.warn("rowkey format error : " + rowKey);
        }
        return jsonFileName.toString();
    }

    /**
     * 通过rowKey解析到数据存储路径
     *
     * @param rowKey rowKey
     * @return filePath 数据存储路径
     */
    public static String key2filePath(String rowKey) {
        String ipcId = rowKey.substring(0, rowKey.indexOf("_"));
        String timeStr = rowKey.substring(rowKey.indexOf("_") + 1, rowKey.length());
        String year = timeStr.substring(0, 2);
        String month = timeStr.substring(2, 4);
        String day = timeStr.substring(4, 6);
        String hour = timeStr.substring(6, 8);
        String minute = timeStr.substring(8, 10);
        //String second = timeStr.substring(10, 12);

        StringBuilder filePath = new StringBuilder();
        filePath = filePath.append("E:\\photo1\\").append(ipcId).
                append("\\20").append(year).append("\\").append(month).append("\\").append(day).
                append("\\").append(hour).append("\\").append(minute);
        return filePath.toString();
    }
}
