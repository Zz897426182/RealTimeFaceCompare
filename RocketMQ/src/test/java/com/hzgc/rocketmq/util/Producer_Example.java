package com.hzgc.rocketmq.util;

import java.util.List;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

public class Producer_Example {
    public static void main(String[] args) {
        String key;
        RocketMQProducer producer = RocketMQProducer.getInstance();
        MessageQueueSelector selector = new MessageQueueSelector() {

            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                String msgid = (String) arg;
                int id = Integer.parseInt(msgid.substring(0, 3) + msgid.substring(msgid.length() - 3));
                int queuqid = id % mqs.size();
                return mqs.get(queuqid);
            }
        };

        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                key = "0000ABC00" + i;
            } else if (i < 100) {
                key = "0000ABC0" + i;
            } else {
                key = "0000ABC" + i;
            }

            producer.send("TopicTest2", "TagF", key, ("Message Test " + i).getBytes(), selector);

        }

        producer.shutdown();

    }
}
