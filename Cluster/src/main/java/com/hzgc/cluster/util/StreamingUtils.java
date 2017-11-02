package com.hzgc.cluster.util;

import com.hzgc.util.FileUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StreamingUtils implements Serializable {
    public static Integer getSimilarity(Map<String, Integer> map) {
        for (String key : map.keySet()) {
            if (map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    public static Properties getProperties() {
        Properties ps = new Properties();
        try {
            System.out.println(FileUtil.loadResourceFile("."));
            InputStream is = new FileInputStream(FileUtil.loadResourceFile("sparkJob.properties"));
            ps.load(is);
        } catch (Exception e) {
            System.out.println(e);
        }
        return ps;
    }

    /**
     * 字节数组转化为字符串
     *
     * @param b
     * @return
     * @throws Exception
     */
    public static String byteArray2string(byte[] b) throws Exception {
        String str = new String(b, "ISO-8859-1");
        return str;
    }

    /**
     * scala 数组转化为Java list（刘善彬 To 内）
     *
     * @param array
     * @return
     */
    public static List<String> arrayBuffer2javaList(String[] array) {
        return Arrays.asList(array);
    }

    /**
     * java list 转化为Scala数组
     *
     * @param list
     * @return
     */
    public static String[] javaList2arrayBuffer(List list) {
        String[] str = new String[list.size()];
        return (String[]) list.toArray(str);
    }

    /**
     * 获取离线告警更新时间距离与当前时间的天数（刘善彬 To 内）
     *
     * @param updateTime 告警更新时间（时间格式：yyyy-MM-dd HH:mm:ss）
     * @return 距当前时间的天数
     */
    public static String timeTransition(String updateTime) {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //更新时间
            Date date = sf.parse(updateTime);
            long updateT = date.getTime();
            //当前时间
            Date d = new Date(System.currentTimeMillis());
            long currentTime = d.getTime();
            //一天的毫秒数
            long t = 1000 * 60 * 60 * 24;
            long interval = currentTime - updateT;
            Double days = interval * 1.0 / t;
            DecimalFormat df = new DecimalFormat("######0.00");
            return df.format(days);

        } catch (ParseException e) {
            System.out.println(e.toString());

        }
        return null;
    }

    public static List<String> getOffLineArarmObjType(Map map) {
        List<String> list = new ArrayList<String>();
        for (Object s : map.keySet()) {
            if (map.get(s) != null && map.get(s).toString().length() > 0) {
                list.add(s.toString());
            }
        }
        return list;
    }
}
