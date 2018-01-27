package com.hzgc.ftpserver.captureSubscription;

import com.hzgc.ftpserver.util.ZookeeperClient;
import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 人脸抓拍订阅定时刷新及去除过期数据
 */
public class MQSwitchStart extends CaptureSubscriptionObject{
    private static Logger LOG = Logger.getLogger(MQSwitchStart.class);
    private static ZookeeperClient zookeeperClient;
    private static String path = MQSwitchInit.getPath();
    private CaptureSubscriptionObject object = new CaptureSubscriptionObject();

    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties")));
            String zookeeperAddress = properties.getProperty("zookeeperAddress");
            zookeeperClient = new ZookeeperClient(30000, zookeeperAddress, path, false);
        } catch (IOException e) {
            LOG.error("zookeeperAddress no found in the \"rocketmq.properties\"");
            e.printStackTrace();
        }
    }

    public void start() {
        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    captureSubscription = zookeeperClient.getMQData();
                    object.setIpcIdList(captureSubscription);
                    long current = System.currentTimeMillis();
                    LOG.info("777" + "time:" + current + ", captureSubscription:" + captureSubscription + ", IPCID:" + object.getIpcIdList());
                    for (String userId : captureSubscription.keySet()) {
                        Map<String, List<String>> map = captureSubscription.get(userId);
                        for (String time : map.keySet()) {
                            long failureTime = Long.parseLong(time) + 60 * 60 * 24 * 180;
                            if (failureTime >= current) {
                                zookeeperClient.delete(path + "/" + userId);
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error("MQ Switch thread error!");
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
        LOG.info("MQ Switch started!");
    }
}
