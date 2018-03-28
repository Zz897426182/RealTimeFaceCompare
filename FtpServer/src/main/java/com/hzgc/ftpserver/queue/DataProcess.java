package com.hzgc.ftpserver.queue;

import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.ftpserver.util.FtpUtils;
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
    private final Logger LOG = LoggerFactory.getLogger(DataProcess.class);

    private final int threadNum = Integer.valueOf(QueueUtil.getProperties("cluster-over-ftp.properties").getProperty("thread.number"));

    private final String homeDirectory = QueueUtil.getProperties("users.properties").getProperty("com.hzgc.ftpserver.user.admin.homedirectory");

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ProducerOverFtp kafkaProducer = ProducerOverFtp.getInstance();

    public void reader() {
        try {
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);
            BlockingQueue queue = BufferQueue.getInstance().getQueue();
            for (int i = 0; i < threadNum; i++) {
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                String fileName = queue.take().toString();
                                Map<String, String> map = FtpUtils.getFtpPathMessage(fileName);
                                String ipcID = map.get("ipcID");
                                String timeStamp = map.get("time");
                                String date = map.get("date");
                                String timeSlot = map.get("sj");

                                FaceObject faceObject = new FaceObject();
                                faceObject.setIpcId(ipcID);
                                faceObject.setTimeStamp(timeStamp);
                                faceObject.setTimeSlot(timeSlot);
                                faceObject.setDate(date);
                                faceObject.setType(SearchType.PERSON);
                                faceObject.setStartTime(sdf.format(new Date()));
                                byte[] data = QueueUtil.getData(fileName, homeDirectory);
                                if (data != null && data.length != 0) {
                                    faceObject.setAttribute(FaceFunction.featureExtract(data));
                                    String ftpUrl = FtpUtils.filePath2FtpUrl(fileName);
                                    kafkaProducer.sendKafkaMessage(ProducerOverFtp.getFEATURE(), ftpUrl, faceObject);
                                    LOG.info("Send to kafka success, queue size:" + queue.size());
                                } else {
                                    LOG.info(fileName + " picture data is null");
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
