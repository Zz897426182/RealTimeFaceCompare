package com.hzgc.dubbo.clustering;

import java.io.Serializable;

/**
 * 某个聚类中新增告警详细信息，包含大小图url，告警时间（彭聪）
 */
public class AlarmInfo implements Serializable {
    /**
     * 小图URL
     */
    private String surl;
    /**
     * 大图URL
     */
    private String burl;
    /**
     * 告警时间（MySQL中保存的告警时间）
     */
    private String alarmTime;

    public String getSurl() {
        return surl;
    }

    public void setSurl(String surl) {
        this.surl = surl;
    }

    public String getBurl() {
        return burl;
    }

    public void setBurl(String burl) {
        this.burl = burl;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
}
