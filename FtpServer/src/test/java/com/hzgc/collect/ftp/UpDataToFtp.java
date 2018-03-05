package com.hzgc.collect.ftp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.collect.expand.util.FTPDownloadUtils;

import java.io.File;
import java.util.Random;
import java.util.concurrent.*;

public class UpDataToFtp {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.execute(new upDataThread());

    }
}

/**
 * 对于本地path路径下的所有文件，循环loopNum次，发送到Ftp服务器
 */
class upDataThread implements Runnable{

    //counter：计数器；在需要统计数据的位置调用inc()和dec()方法。
    private static MetricRegistry metricRegistry = new MetricRegistry();
    private final static Counter counter = metricRegistry.counter("counter");

    String path = "/home/test/picFrom"; //图片路径
    int loopNum = 1; //循环次数
    String IpcId = "DS-2DE72XYZIW-ABCVS20160823CCCH641752612"; //ipcId

    @Override
    public void run() {
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < loopNum; i++) {
            Random random = new Random();
            int randNum = random.nextInt(10000000);
            String randName = String.valueOf(randNum);
            for (int j = 0; j < ( tempList != null ? tempList.length : 0); j++) {
                if (tempList[j].isFile()){
                    String originFilePath = tempList[j].getAbsolutePath();
                    String fileName = randName + tempList[j].getName();
                    StringBuilder filePath = new StringBuilder();
                    //拼接路径
                    filePath = filePath.append(IpcId).append("/")
                            .append(tempList[j].getName().substring(0, 14).replaceAll("_","/"));

                    //basePath FTP服务器基础目录
                    //filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。
                    //文件的路径为 basePath + filePath
                    FTPDownloadUtils.upLoadFromProduction("172.18.18.163", 2222, "admin",
                            "123456", "", filePath.toString(), fileName, originFilePath);
                    counter.inc();
                }
            }
        }
        System.out.println("Thread name is: " + Thread.currentThread().getName() + "pic count send to FTP is：" + counter.getCount());
    }
}