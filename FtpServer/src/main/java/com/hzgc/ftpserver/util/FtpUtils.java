package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.FTP;
import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FtpUtils implements Serializable {
    private static Logger LOG = Logger.getLogger(FtpUtils.class);

    private static Properties properties = new Properties();

    static {
        FileInputStream in = null;
        try {
            in = new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties"));
            properties.load(in);
            int ftpServerPort = Integer.parseInt(properties.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * 根据文件上传至ftp的绝对路径获取到ipcID、TimeStamp、Date、TimeSlot
     *
     * @param fileName 文件上传至ftp的绝对路径，例如：/3B0383FPAG51511/2017/05/23/16/00/2017_05_23_16_00_15_5704_0.jpg
     * @return 设备、时间等信息 例如：{date=2017-05-23, sj=1600, ipcID=3B0383FPAG51511, time=2017-05-23 16:00:15}
     */
    public static Map<String, String> getFtpPathMessage(String fileName) {
        String ipcID = fileName.substring(1, fileName.indexOf("/", 1));
        String timeStr = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("_")).replace("_", "");

        String year = timeStr.substring(0, 4);
        String month = timeStr.substring(4, 6);
        String day = timeStr.substring(6, 8);
        String hour = timeStr.substring(8, 10);
        String minute = timeStr.substring(10, 12);
        String second = timeStr.substring(12, 14);

        StringBuilder time = new StringBuilder();
        time = time.append(year).append("-").append(month).append("-").append(day).
                append(" ").append(hour).append(":").append(minute).append(":").append(second);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder date = new StringBuilder();
        date = date.append(year).append("-").append(month).append("-").append(day);

        StringBuilder sj = new StringBuilder();
        sj = sj.append(hour).append(minute);

        Map<String, String> map = new HashMap<>();
        try {
            /*Date date = sdf.parse(time.toString());
            long timeStamp = date.getTime();*/
            map.put("ipcID", ipcID);
            map.put("time", time.toString());
            map.put("date", date.toString());
            map.put("sj", sj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据ftpUrl获取到IP、HostName、ipcID、TimeStamp、Date、TimeSlot
     *
     * @param ftpUrl ftp地址 例如：ftp://172.18.18.109:2121/ABCVS20160823CCCH/2017_11_09_10_53_35_2_0.jpg
     * @return 设备、时间等信息 例如：{date=2017-11-09, filepath=/ABCVS20160823CCCH/2017_11_09_10_53_35_2_0.jpg, port=2121, ip=172.18.18.109
     * , timeslot=1053, ipcid=ABCVS20160823CCCH, timestamp=2017-11-09 10:53:35}
     */
    public static Map<String, String> getFtpUrlMessage(String ftpUrl) {
        Map<String, String> map = new HashMap<>();
        String ip = ftpUrl.substring(ftpUrl.indexOf(":") + 3, ftpUrl.lastIndexOf(":"));
        String portStr = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
        String port = portStr.substring(0, portStr.indexOf("/"));
        String filePath = portStr.substring(portStr.indexOf("/"));
        Map<String, String> filePathMap = getFtpPathMessage(filePath);
        if (!filePathMap.isEmpty()) {
            String ipcID = filePathMap.get("ipcID");
            String timeStamp = filePathMap.get("time");
            String date = filePathMap.get("date");
            String timeSlot = filePathMap.get("sj");
            map.put("ip", ip);
            map.put("port", port);
            map.put("filepath", filePath);
            map.put("ipcid", ipcID);
            map.put("timestamp", timeStamp);
            map.put("date", date);
            map.put("timeslot", timeSlot);
        }
        return map;
    }

    /**
     * 通过上传文件路径解析到文件的ftp地址（ftp发送至kafka的key）
     *
     * @param filePath ftp接收数据路径
     * @return 文件的ftp地址
     */
    public static String filePath2FtpUrl(String filePath) {
        StringBuilder url = new StringBuilder();
        String hostName = IPAddressUtils.getHostName();
        Map<Integer, Integer> ftpPIDMap = FTP.getPidMap();
        if (!ftpPIDMap.isEmpty()){
            Integer ftpPID = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            //LOG.info("ftp PID = " + ftpPID);
            int ftpPort = ftpPIDMap.get(ftpPID);
            url = url.append("ftp://").append(hostName).append(":").append(ftpPort).append(filePath);
            return url.toString();
        }
        url = url.append("ftp://").append(hostName).append(":").append("none").append(filePath);
        return url.toString();
    }


    /**
     * 小图ftpUrl转大图ftpUrl
     *
     * @param surl 小图ftpUrl
     * @return 大图ftpUrl
     */
    public static String surlToBurl(String surl) {
        StringBuilder burl = new StringBuilder();
        String s1 = surl.substring(0, surl.lastIndexOf("_") + 1);
        String s2 = surl.substring(surl.lastIndexOf("."));
        burl.append(s1).append(0).append(s2);
        return burl.toString();
    }

    /**
     * ftpUrl中的HostName转为IP
     *
     * @param ftpUrl 带HostName的ftpUrl
     * @return 带IP的ftpUrl
     */
    public static String getFtpUrl(String ftpUrl) {
        String hostName = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
        String ftpServerIP = properties.getProperty(hostName);
        if (ftpServerIP != null && ftpServerIP.length() > 0)
            return ftpUrl.replace(hostName, ftpServerIP);
        return ftpUrl;
    }
}
