package com.hzgc.collect.ftp;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.hzgc.collect.expand.util.FTPDownloadUtils;
import org.junit.Test;

import java.io.File;
import java.util.Random;

public class SendDataTo2222Ftp {

    //counter：计数器；在需要统计数据的位置调用inc()和dec()方法。
    private static MetricRegistry metricRegistry = new MetricRegistry();
    private final static Counter counter = metricRegistry.counter("counter");


    /**
     *
     * 对于本地path路径下的所有文件，循环loopNum次，发送到Ftp服务器
     *
     * @param path 文件路径
     * @param loopNum 循环次数
     * @param IpcId 设备ID
     */
    public static void upDataTest(String path, int loopNum, String IpcId){
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

                    System.out.println(filePath);
                    System.out.println(fileName);
                    System.out.println(originFilePath);

                    counter.inc();
                    System.out.println(counter.getCount());
                }
            }
        }
        System.out.println("发送到ftp的图片数量：" + counter.getCount());
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        upDataTest("/home/test/picFrom",1,"DS-2DE72XYZIW-ABCVS20160823CCCH641752612");
        System.out.println("++++++++++++++++++++++++++++");
        System.out.println(System.currentTimeMillis() - start);
    }


}
