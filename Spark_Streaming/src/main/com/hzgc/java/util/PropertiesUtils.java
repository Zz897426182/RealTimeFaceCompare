package com.hzgc.java.util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by 刘善彬 on 2017/7/17.
 * 资源文件工具类
 */
public class PropertiesUtils {
    /**
     * 通过key获取资源文件的value
     * @param key 资源文件的key
     * @return 返回key对应资源文件的value
     * @throws Exception
     */
    public static String getPropertiesValue(String key) throws Exception{
        InputStream is = PropertiesUtils.class.getResourceAsStream("/bigData.properties");
        Properties ps = new Properties();
        ps.load(is);
        String value = ps.getProperty(key);
        return value;
    }

}
