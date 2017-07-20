package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.local.LocalOverFtpServer;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

public class Utils {
    private static Logger log = Logger.getLogger(Utils.class);
    public static File jsonLogPath;

    public static boolean checkPort(int checkPort) throws Exception {
        return checkPort > 1024;
    }
    

    public static File loadResourceFile(String resourceName) throws Exception{
        if (false) {
            URL resource = LocalOverFtpServer.class.getResource("/conf/");
            String confPath = resource.getPath();
            confPath = confPath.substring(5, confPath.lastIndexOf("/lib"));
            confPath = confPath + "/conf/";
            System.out.println(confPath);
            File sourceFile = new File(confPath + resourceName);
            PropertyConfigurator.configure(confPath + "/com/hzgc/com.hzgc.ftpserver/log4j.properties");
            PropertyConfigurator.configure(confPath + "/hbase-site.xml");
            if (!sourceFile.exists()) {
                log.error("The local resource file:" + new File(confPath).getAbsolutePath()
                        + "/" + resourceName + " is not found, " +
                        "please check it, System exit.");
                System.exit(1);
            }
            log.info("The resource file:" + new File(confPath).getAbsolutePath() + "was load successfull");
            return sourceFile;
        } else {
            URL resource = LocalOverFtpServer.class.
                    getResource("/conf/" + resourceName);
            if (resource != null) {
                return new File(resource.getFile());
            }
        }
        log.error("Can not find rsource file:/conf" + resourceName);
        return null;
    }


    public static String loadJsonFile(InputStream is) {
//        BufferedInputStream bis;
        BufferedReader reader = null;
        StringBuilder strBuff = new StringBuilder();
        try {
//            bis = new BufferedInputStream(is);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            reader = new BufferedReader(isr);
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                strBuff.append(tempStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                IoUtils.close(reader);
            }
        }
        return strBuff.toString();
    }

    public static void writeJsonLog(String jsonContext) {
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(jsonLogPath, true);
            printWriter = new PrintWriter(fileWriter);
            printWriter.println(jsonContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(printWriter);
            IoUtils.close(fileWriter);
        }
    }

    public static void analysisJsonFile(String jsonContext) {
        JSONArray jsonArray = new JSONArray(jsonContext);
        int jsonSize = jsonArray.length();
        System.out.println("Size:" + jsonSize);
        for (int i = 0; i < jsonSize; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Iterator iterable = jsonObject.keys();
            while (iterable.hasNext()) {
                String key = (String) iterable.next();
                String value = jsonObject.get(key).toString();
                if (value.startsWith("[") && value.endsWith("]")) {
                    analysisJsonFile(value);
                }
                System.out.println(key + "=" + value);
            }
        }
    }

    public static ByteArrayOutputStream inputStreamCacher(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            while((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            IoUtils.close(baos);
            IoUtils.close(is);
        }
        return baos;
    }

    /**
     *
     * @param pictureName
     * determine the picture type based on the file name
     * @return
     * equals 0, it is a picture
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

    public static String faceNum(String facekey){
        String column = facekey.substring(facekey.lastIndexOf("_") + 1,facekey.length());
        return column;
    }

    public static String faceRowKey(String faceKey){
        String rowKeyStr = faceKey.substring(0,faceKey.lastIndexOf("_"));
        StringBuilder rowkey = new StringBuilder();
        rowkey.append(rowKeyStr);
        int rowKeyLen = rowkey.length();
        if (rowkey.length() < 48){
            for (int i = 0; i < 48 - rowKeyLen; i++){
                rowkey.insert(rowKeyLen ,"0");
            }
        }
        System.out.println("faceRowKey = " + rowkey.toString() + "  faceRowKey.length() = " + rowkey.length());
        return rowkey.toString();
    }

    public static String faceKey(int faceNum,String key){
        StringBuilder faceNumStr = new StringBuilder();
        faceNumStr.append(faceNum);
        String faceKeyStr = key.substring(0,key.length() - faceNumStr.length() - 1);
        StringBuilder faceKey = new StringBuilder();
        faceKey.append(faceKeyStr).append("_").append(faceNum);
        System.out.println("faceKey = " + faceKey.toString() + "  faceKey.length() = " + faceKey.length());
        return faceKey.toString();
    }
    public static String transformNameToKey(String fileName) {
        StringBuilder key = new StringBuilder();

        if(null != fileName && fileName.length() >0){
            String ipcID = fileName.substring(1,fileName.lastIndexOf("/"));
            String tempKey = fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf("_")).replace("/","");
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(0, tempKey.lastIndexOf("_")).replace("_", "");

            key = key.append(ipcID).append("_").append(timeName).append("_");
            StringBuffer prefixNameKey = new StringBuffer();
            prefixNameKey = prefixNameKey.append(prefixName).reverse();
            if (prefixName.length() < 10){
                for (int i = 0; i < 10 - prefixName.length(); i++){
                    prefixNameKey.insert(0,"0");
                }
                key.append(prefixNameKey).append("_");
            }else {
                key.append(prefixName).append("_");
            }

            if (key.length() < 48){
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < 48 - key.length(); i++){
                    stringBuffer.insert(0,"0");
                }
                key.append(stringBuffer);
            }
        }else {
            key.append(fileName);
        }
        System.out.println("key = " + key + " key.length() = " + key.length());
        return key.toString();
    }
}
