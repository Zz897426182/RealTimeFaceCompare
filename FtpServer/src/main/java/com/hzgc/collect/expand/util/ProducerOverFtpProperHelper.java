package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * 从配置文件producer-over-ftp.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class ProducerOverFtpProperHelper extends ProperHelper implements Serializable {
    private static Logger LOG = Logger.getLogger(ProducerOverFtpProperHelper.class);
    private static Properties props = new Properties();

    private static String bootstrapServers;
    //private static String clientId;
    private static String requestRequiredAcks;
    private static String retries;
    private static String keySerializer;
    private static String valueSerializer;
    private static String topicFeature;

    static {
        String properName = "producer-over-ftp.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
                LOG.info("Load configuration for ftp server from ./conf/producer-over-ftp.properties");

                setBootstrapServers();
                //setClientId();
                setRequestRequiredAcks();
                setRetries();
                setKeySerializer();
                setValueSerializer();
                setTopicFeature();
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
    private static void setBootstrapServers() {
        bootstrapServers = verifyIpPlusPortList("bootstrap.servers", props, LOG);
    }


    //private static void setClientId(){
    //clientId = verifyCommonValue("client.id", "p1", props, LOG);
    //}

    private static void setRequestRequiredAcks() {
        requestRequiredAcks = verifyIntegerValue("request.required.acks", "1", props, LOG);
    }

    private static void setRetries() {
        retries = verifyIntegerValue("retries", "0", props, LOG);
    }

    private static void setKeySerializer() {
        keySerializer = verifyCommonValue("key.serializer", "org.apache.kafka.common.serialization.StringSerializer", props, LOG);
    }

    private static void setValueSerializer() {
        valueSerializer = verifyCommonValue("value.serializer", "com.hzgc.ftpserver.producer.FaceObjectEncoder", props, LOG);
    }

    private static void setTopicFeature() {
        topicFeature = verifyCommonValue("topic-feature", "feature", props, LOG);
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */
    public static String getBootstrapServers() {
        return bootstrapServers;
    }

    //public static String getClientId() {
    //return clientId;
    //}
    public static Integer getRequestRequiredAcks() {
        return Integer.valueOf(requestRequiredAcks);
    }

    public static Integer getRetries() {
        return Integer.valueOf(retries);
    }

    public static String getKeySerializer() {
        return keySerializer;
    }

    public static String getValueSerializer() {
        return valueSerializer;
    }

    public static String getTopicFeature() {
        return topicFeature;
    }

    /**
     * 获取Properties属性的资源文件变量
     */
    public static Properties getProps() {
        return props;
    }

}

