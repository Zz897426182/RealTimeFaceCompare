package com.hzgc.collect.expand.processer;


import com.hzgc.collect.expand.conf.CommonConf;
import com.hzgc.collect.expand.log.DataProcessLogWriter;
import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.util.FtpUtils;
import com.hzgc.collect.expand.util.ProducerKafka;
import com.hzgc.collect.expand.util.ProducerOverFtpProperHelper;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
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
                // 判断图片清晰度
                int sharpness = 1;
                try {
                    BufferedImage image = ImageIO.read(new FileInputStream(event.getAbsolutePath()));
                    int imageHeight = image.getHeight();
                    int imageWidth = image.getWidth();
                    if (imageHeight > 80 && imageWidth > 80){
                        sharpness = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FaceAttribute attribute = FaceFunction.featureExtract(event.getAbsolutePath());
                //根据带端口号的ftpurl：例如ftp://s120:2121/DS-2DE72XYZIW-ABCVS2016/2018/02/01/19/582_1.jpg
                //截取到/DS-2DE72XYZIW-ABCVS2016/2018/02/01/19/582_1.jpg
                String portPath =event.getFtpPath();
                String path = portPath.split("://")[1].substring(portPath.split("://")[1].indexOf("/"));
                FtpPathMessage message = FtpUtils.getFtpPathMessage(path);
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
