package com.hzgc.rocketmq.util;

import org.apache.log4j.Logger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class RocketMQProducer implements Serializable {
    private static Logger log = Logger.getLogger(RocketMQProducer.class);
    private static String namesrvAddr;
    private static String topic;
    private static String tag;
    private static String producerGroup;
    private static Properties properties = new Properties();
    private static FileInputStream fis;
    private static RocketMQProducer instance = null;
    private static String path;
    private DefaultMQProducer producer;

    private RocketMQProducer() {
        try {
            /*String classRoute = this.getClass().getResource("/").getPath();
			int index = classRoute.indexOf("RocketMQ");
			String dir = classRoute.substring(0, index);
			path = dir + "Distribution/conf/rocketmq.properties";*/
            path = "RocketMQ/src/main/resources/conf/rocketmq.properties";
            fis = new FileInputStream(new File(path));
            properties.load(fis);
            namesrvAddr = properties.getProperty("namesrvAddr");
            topic = properties.getProperty("topic");
            //tag = properties.getProperty("tag");
            producerGroup = properties.getProperty("producerGroup", UUID.randomUUID().toString());
            if (namesrvAddr == null || topic == null) {
                log.error("parameter init error");
                throw new Exception("parameter init error");
            }
            producer = new DefaultMQProducer(producerGroup);
            producer.setNamesrvAddr(namesrvAddr);
            producer.start();
            log.info("producer started...");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("producer init error...");
            throw new RuntimeException(e);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("fileinputstream close error...");
                    throw new RuntimeException(e);
                }
        }
    }

    public static RocketMQProducer getInstance() {
        if (instance == null) {
            synchronized (RocketMQProducer.class) {
                if (instance == null) {
                    instance = new RocketMQProducer();
                }
            }
        }
        return instance;
    }

    public void send(byte[] data) {
        send(topic, null, null, data, null);
    }

    public void send(String tag, byte[] data) {
        send(topic, tag, null, data, null);
    }

    public void send(String tag, String key, byte[] data) {
        send(topic, tag, key, data, null);
    }

    public void send(String topic, String tag, String key, byte[] data, final MessageQueueSelector selector) {
        try {
            Message msg;
            if (tag == null || tag.length() == 0) {
                msg = new Message(topic, data);
            } else if (key == null || key.length() == 0) {
                msg = new Message(topic, data);
            } else {
                msg = new Message(topic, tag, key, data);
            }

            //long startTime = System.currentTimeMillis();
            SendResult sendResult;
            if (selector != null) {
                sendResult = producer.send(msg, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return selector.select(mqs, msg, arg);
                    }
                }, key);
            } else {
                sendResult = producer.send(msg);
            }
            //log.info(startTime);
            log.info(sendResult);
            System.out.println(sendResult);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("send message error...");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public String getTopic() {
        return topic;
    }

    public String getTag() {
        return tag;
    }

}
