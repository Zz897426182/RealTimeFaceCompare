package com.hzgc.ftpserver.producer;

import com.hzgc.util.common.FileUtil;
import com.hzgc.util.common.IOUtil;
import com.hzgc.util.common.StringUtil;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * FACE_CAPTURE_SWITCH
 */
public class RocketMQConsumer {
    private static Logger LOG = Logger.getLogger(RocketMQConsumer.class);
    private static String captureSwitchTopic;
    private static Properties properties = new Properties();
    private static RocketMQConsumer instance = null;
    private static DefaultMQPushConsumer consumer;

    private RocketMQConsumer(){
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties"));
            properties.load(fis);
            String namesrvAddr = properties.getProperty("address");
            captureSwitchTopic = properties.getProperty("CaptureSwitchTopic");
            String producerGroup = properties.getProperty("group", UUID.randomUUID().toString());
            if (StringUtil.strIsRight(namesrvAddr) && StringUtil.strIsRight(captureSwitchTopic) && StringUtil.strIsRight(producerGroup)) {
                consumer = new DefaultMQPushConsumer();
                consumer.setNamesrvAddr(namesrvAddr);
                consumer.subscribe(captureSwitchTopic,"OPEN || CLOSE");
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                LOG.info("RocketMQConsumer started...");
            } else {
                LOG.error("RocketMQConsumer param init error");
            }
        } catch (IOException | MQClientException e) {
            LOG.error("RocketMQConsumer init error...");
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(fis);
        }
    }

    public static RocketMQConsumer getInstance() {
        if (instance == null) {
            synchronized (RocketMQConsumer.class) {
                if (instance == null) {
                    instance = new RocketMQConsumer();
                }
            }
        }
        return instance;
    }

    public static void run(){
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                return null;
            }
        });
    }

}
