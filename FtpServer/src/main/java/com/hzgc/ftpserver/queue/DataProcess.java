package com.hzgc.ftpserver.queue;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.feature.FaceAttribute;
import com.hzgc.ftpserver.common.FtpUtil;
import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.ftpserver.producer.ProducerOverFtp;
import com.hzgc.jni.FaceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列数据处理
 */
public class DataProcess {

    public static void reader() {
        try {
            int threadNum = Integer.valueOf(QueueUtil.getProperties("cluster-over-ftp.properties").getProperty("thread.number"));
            String homedirectory = QueueUtil.getProperties("users.properties").getProperty("com.hzgc.ftpserver.user.admin.homedirectory");
            final Logger LOG = LoggerFactory.getLogger(DataProcess.class);
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
            BlockingQueue queue = BufferQueue.getInstance().getQueue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ProducerOverFtp kafkaProducer = ProducerOverFtp.getInstance();
            for (int i = 0; i < threadNum; i++) {
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                String fileName = queue.take().toString();
                                int size = queue.size();
                                String ftpUrl = FtpUtil.filePath2absolutePath(fileName);
                                Map<String, String> map = FtpUtil.getFtpPathMessage(fileName);
                                String ipcID = map.get("ipcID");
                                String timeStamp = map.get("time");
                                String date = map.get("date");
                                String timeSlot = map.get("sj");
                                byte[] data = QueueUtil.getData(fileName, homedirectory);
                                FaceObject faceObject = new FaceObject();
                                faceObject.setIpcId(ipcID);
                                faceObject.setTimeStamp(timeStamp);
                                faceObject.setTimeSlot(timeSlot);
                                faceObject.setDate(date);
                                faceObject.setType(SearchType.PERSON);
                                if (data != null && data.length != 0) {
                                    FaceAttribute attribute = FaceFunction.featureExtract(data);
                                    float[] tz = attribute.getFeature();
                                    faceObject.setAttribute(attribute);
                                    faceObject.setStartTime(sdf.format(new Date()));
                                    kafkaProducer.sendKafkaMessage(ProducerOverFtp.getFEATURE(), ftpUrl, faceObject);
                                    LOG.info("Send to kafka success,queue size : " + size);
                                } else {
                                    LOG.info(fileName + " Small picture data is null");
                                }

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
