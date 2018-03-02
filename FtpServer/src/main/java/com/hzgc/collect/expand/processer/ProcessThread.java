package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.util.FtpUtils;
import com.hzgc.collect.expand.util.ProducerKafka;
import com.hzgc.collect.expand.util.ProducerOverFtpProperHelper;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

public class ProcessThread implements Runnable {
    private static Logger LOG = Logger.getLogger(ProcessThread.class);
    private BlockingQueue<LogEvent> queue;
    private DataProcessLogWriter writer;

    public ProcessThread(CommonConf conf, BlockingQueue<LogEvent> queue, String queueID) {
        this.queue = queue;
        writer = new DataProcessLogWriter(conf, queueID);
    }

    @Override
    public void run() {
        LogEvent event;
        try {
            while ((event = queue.take()) != null) {
                FaceAttribute attribute = FaceFunction.featureExtract(event.getAbsolutePath());
                FtpPathMessage message = FtpUtils.getFtpPathMessage(event.getFtpPath());
                if (attribute.getFeature() != null) {
                    FaceObject faceObject = new FaceObject(message.getIpcid()
                            , message.getTimeStamp()
                            , SearchType.PERSON
                            , message.getDate()
                            , message.getTimeslot()
                            , attribute
                            , event.getTimeStamp() + "");
                    ProcessCallBack callBack = new ProcessCallBack(event.getFtpPath(),
                            System.currentTimeMillis(), this.writer, event);
                    ProducerKafka.getInstance().sendKafkaMessage(
                            ProducerOverFtpProperHelper.getTopicFeature(),
                            event.getFtpPath(),
                            faceObject,
                            callBack);
                } else {
                    writer.countCheckAndWrite(event);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
