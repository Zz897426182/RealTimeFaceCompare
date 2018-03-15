package com.hzgc.collect.expand.processer;

import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

public class ProcessCallBack implements Callback {
    private static Logger LOG = Logger.getLogger(ProcessCallBack.class);
    private DataProcessLogWriter writer;
    private LogEvent event;
    private long elapsedTime;
    private String key;

    ProcessCallBack(String key, long time, DataProcessLogWriter writer, LogEvent event) {
        this.writer = writer;
        this.event = event;
        this.key = key;
        this.elapsedTime = time;
    }
    @Override
    public void onCompletion(RecordMetadata metadata, Exception e) {
        if (metadata != null) {
            event.setTimeStamp(System.currentTimeMillis());
            writer.countCheckAndWrite(event);
            LOG.info("Send Kafka successfully! message:[topic:" + metadata.topic() + ", key:" + key +
                    "], send to partition(" + metadata.partition() + "), offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
        } else {
            event.setTimeStamp(System.currentTimeMillis());
            writer.countCheckAndWrite(event);
            writer.errorLogWrite(writer.getErrorLog(), this.event);
            LOG.error("Send Kafka failed！ message:[" + key + "], write to error log！");
            LOG.error(e.getMessage());
        }
    }
}
