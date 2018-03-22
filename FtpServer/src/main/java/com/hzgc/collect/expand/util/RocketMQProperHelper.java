package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * 从配置文件rocketmq.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class RocketMQProperHelper extends ProperHelper implements Serializable {
    private static Logger LOG = Logger.getLogger(RocketMQProperHelper.class);
    private static Properties props = new Properties();
    private static String address;
    private static String topic;
    private static String group;

    static {
        String properName = "rocketmq.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
                LOG.info("Load configuration for ftp server from ./conf/rocketmq.properties");

                setAddress();
                setTopic();
                setGroup();
            } else {
                LOG.error("The property file " + properName + "doesn't exist!");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Catch an unknown error, can't load the configuration file" + properName);
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

    /**
     * set方法。验证配置文件中的值是否为符合条件的格式。
     */
    private static void setAddress() {
        address = verifyIpPlusPort("address", props, LOG);
    }

    private static void setTopic() {
        topic = verifyCommonValue("topic", "REALTIME_PIC_MESSAGE", props, LOG);
    }

    private static void setGroup() {
        group = verifyCommonValue("group", "FaceGroup", props, LOG);
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public static String getAddress() {
        return address;
    }

    public static String getTopic() {
        return topic;
    }

    public static String getGroup() {
        return group;
    }

    /**
     * 获取Properties属性的资源文件变量
     */
    public static Properties getProps() {
        return props;
    }

}
