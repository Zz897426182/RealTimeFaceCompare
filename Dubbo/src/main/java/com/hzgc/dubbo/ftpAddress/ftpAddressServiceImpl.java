package com.hzgc.dubbo.ftpAddress;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ftpAddressServiceImpl implements ftpAddressService {

    private static HashMap<String, String> ftpAddressMap = new HashMap<>();
    private static String ftpAddress;
    private static String ftpPort;
    private static Properties properties = new Properties();
    private static FileInputStream fis;

    static {
        try {
            fis = new FileInputStream("dubbo/src/main/resources/conf/ftpAddress.properties");
            properties.load(fis);
            ftpAddress = properties.getProperty("ftpAddress");
            ftpPort = properties.getProperty("ftpPort");
            ftpAddressMap.put(ftpAddress, ftpPort);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
        }
    }

    public Map<String, String> getftpAddress() {
        return ftpAddressMap;
    }

}
