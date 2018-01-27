package com.hzgc.ftpserver.captureSubscription;

import com.hzgc.ftpserver.util.ZookeeperClient;
import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Zookeeper客户端初始化
 */
public class MQSwitchInit {
    private static Logger LOG = Logger.getLogger(MQSwitchInit.class);
    protected static String path = "/mq_ipcid";
    protected static ZookeeperClient zookeeperClient;

    static {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties")));
            String zookeeperAddress = properties.getProperty("zookeeperAddress");
            zookeeperClient = new ZookeeperClient(1000, zookeeperAddress, path, false);
        } catch (IOException e) {
            LOG.error("zookeeperAddress no found in the \"rocketmq.properties\"");
            e.printStackTrace();
        }
    }

    public static String getPath() {
        return path;
    }

    /**
     * 创建MQ存储节点
     */
    public void create() {
        zookeeperClient.create();
    }
}
