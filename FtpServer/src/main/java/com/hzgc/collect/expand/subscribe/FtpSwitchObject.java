package com.hzgc.collect.expand.subscribe;

import com.hzgc.collect.expand.util.ClusterOverFtpProperHelper;

import java.io.Serializable;

/**
 * FTP接收数据总开关
 */
public class FtpSwitchObject implements Serializable{
    private static boolean ftpSwitch;

    public FtpSwitchObject(){
        ftpSwitch = Boolean.parseBoolean(ClusterOverFtpProperHelper.getFtpSwitch());
    }

    public static boolean isFtpSwitch() {
        return ftpSwitch;
    }
}
