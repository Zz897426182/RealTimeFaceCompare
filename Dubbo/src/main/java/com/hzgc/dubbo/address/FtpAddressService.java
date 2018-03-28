package com.hzgc.dubbo.address;

import java.util.Properties;

public interface FtpAddressService {

    /**
     * 获取Ftp地址和端口号
     *
     * @return key = ip,返回地址；key = port,返回端口号;key = user,返回账户名;key = password,返回密码
     */
    Properties getFtpAddress();

    /**
     * 通过主机名获取FTP的IP地址
     *
     * @param hostname 主机名
     * @return IP地址
     */
    String getIPAddress(String hostname);

}
