package com.hzgc.collect;

import com.hzgc.collect.expand.util.FTPDownloadUtils;

import java.io.File;
import java.util.Random;
import java.util.concurrent.*;

public class UpDataToFtp {
    public static void main(String[] args) {
        UpDataToFtpProperHelper upDataToFtpProperHelper = new UpDataToFtpProperHelper();
        int threadNum = upDataToFtpProperHelper.getThreadNum();

        int loopNum = upDataToFtpProperHelper.getLoopNum(); //发送图片循环次数
        String path = "/home/test/picFrom"; //图片路径
        String ipcId = "DS-2DE72XYZIW-ABCVS20160823CCCH641752612"; //ipcId

        long startTime = System.currentTimeMillis();
        //开启threadNum个线程池来向ftp发送图片
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            UpDataThread upDataThread = new UpDataThread(path, loopNum, ipcId);
            Thread thread = new Thread(upDataThread);
            pool.submit(thread);
        }
        pool.shutdown();

        while (true) {
            if (pool.isTerminated()) {
                long endTime = System.currentTimeMillis();
                System.out.println("The send start time is: " + startTime);
                System.out.println("The send end time is: " + endTime);
                System.out.println("********************************");
                System.out.println("The Throughput is: " + (endTime - startTime));
                System.out.println("********************************");
                break;
            }
        }

        //总共发送到ftp的图片数量
        System.out.println("The total pic count send to FTP is: " + threadNum * (10 * loopNum));
        System.out.println("End");
    }
}

/**
 * 对于本地path路径下的所有文件，循环loopNum次，发送到Ftp服务器
 */
class UpDataThread implements Runnable {
    //从配置文件读取发送图片的端口号
    private UpDataToFtpProperHelper upDataToFtpProperHelper = new UpDataToFtpProperHelper();
    private int port = upDataToFtpProperHelper.getPort();
    private String ip = upDataToFtpProperHelper.getIp();

    private String path; //图片路径
    private int loopNum; //循环次数
    private String ipcId; //ipcId
    private int count;

    UpDataThread(String path, int loopNum, String ipcId) {
        this.path = path;
        this.loopNum = loopNum;
        this.ipcId = ipcId;
    }

    @Override
    public void run() {
        File file = new File(path);
        File[] tempList = file.listFiles();
        for (int i = 0; i < loopNum; i++) {
            Random random = new Random();
            int randNum = random.nextInt(10000000);
            String randName = String.valueOf(randNum);
            for (int j = 0; j < (tempList != null ? tempList.length : 0); j++) {
                if (tempList[j].isFile()) {
                    String originFilePath = tempList[j].getAbsolutePath();
                    String fileName = randName + tempList[j].getName();
                    StringBuilder filePath = new StringBuilder();
                    //拼接路径
                    filePath = filePath.append(ipcId).append("/")
                            .append(tempList[j].getName().substring(0, 14).replaceAll("_", "/"));

                    //basePath FTP服务器基础目录
                    //filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。
                    //文件的路径为 basePath + filePath
                    FTPDownloadUtils.upLoadFromProduction(ip, port, "admin",
                            "123456", "", filePath.toString(), fileName, originFilePath);
                    count++;
                    System.out.println(Thread.currentThread().getName() + ", count: " + count);
                }
            }
        }
        System.out.println("Thread name is: " + Thread.currentThread().getName() + ", Picture count send to FTP is：" + count);
    }
}