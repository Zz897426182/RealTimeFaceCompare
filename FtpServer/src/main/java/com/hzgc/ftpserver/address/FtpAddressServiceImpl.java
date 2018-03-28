package com.hzgc.ftpserver.address;

import com.hzgc.dubbo.address.FtpAddressService;
import com.hzgc.util.common.FileUtil;
import com.hzgc.util.common.IOUtil;

import java.io.*;
import java.util.Properties;

public class FtpAddressServiceImpl implements FtpAddressService, Serializable {
    private static Properties proper = new Properties();

    public FtpAddressServiceImpl() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties"));
            proper.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(fis);
        }
    }

    @Override
    public Properties getFtpAddress() {
        return proper;
    }

    /**
     * 通过主机名获取FTP的IP地址
     *
     * @param hostname 主机名
     * @return IP地址
     */
    @Override
    public String getIPAddress(String hostname) {
        String ftpIpAddress = "";
        if (hostname != null && hostname.length() > 0) {
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
                properties.load(inputStream);
                ftpIpAddress = properties.getProperty(hostname);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeStream(inputStream);
            }
        }
        return ftpIpAddress;
    }
}
