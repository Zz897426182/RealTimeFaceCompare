package com.hzgc.collect.ftp;

import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.merge.MergeUtil;
import com.hzgc.collect.expand.util.JSONHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class EfficiencyOfFtp {


    public static void main(String[] args) {
        String receivePath = "/home/test/data/receive";
        String processPath = "/home/test/data/process";

        Long minReceiveTime = GetFirstEvent(receivePath);
        Long maxProcessTime = GetLastEvent(processPath);
//        Long maxReceiveTime = GetLastEvent(receivePath);
//
//        System.out.println(maxProcessTime);
//        System.out.println(maxReceiveTime);


        System.out.println("FTP receive and process TIME is:" + (maxProcessTime - minReceiveTime));

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }

    /**
     * 获取日志中的最早时间（3000.log的第一行）
     *
     * @param path 路径，例如/home/test/data/receive/
     */
    private static Long GetFirstEvent(String path) {
        MergeUtil mergeUtil = new MergeUtil();
        File file = new File(path);
        File[] files = file.listFiles();
        List<Long> firstTimeStamps = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                File[] logFiles = files[i].listFiles();
                Map<Integer, String> logNameMap = new HashMap<>();
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        String logName = logFile.getName().replace(".log", "");
                        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                        //判断文件名是否是整数，并排除读到error/error.log的可能性
                        if (pattern.matcher(logName).matches() && !logName.contains("error")) {
                            int logNameInt = Integer.parseInt(logName);
                            logNameMap.put(logNameInt, logFile.toString());
                        }
                    }
                    //获取3000.log的第一行（日志的最早时间）
                    String earliestFilePath;
                    // 若process文件夹下除了0000.log有其他的日志，则最早日志为3000.log；否则为0000.log
                    if (logFiles.length > 1) {
                        logNameMap.remove(0000000000000000000);
                        int minFileName = Collections.min(logNameMap.keySet());
                        earliestFilePath = logNameMap.get(minFileName);
                    } else { // 若process文件夹下只有0000.log
                        earliestFilePath = logNameMap.get(0000000000000000000);
                    }
//                    System.out.println(earliestFilePath);

                    // content of 0000003000.log
                    List<String> firstLogContent = mergeUtil.getAllContentFromFile(earliestFilePath);
                    // last event of 000000003000.log
                    LogEvent logEvent = JSONHelper.toObject(firstLogContent.get(0), LogEvent.class);
                    Long timeStamp = logEvent.getTimeStamp();
                    firstTimeStamps.add(timeStamp);
//                    System.out.println(timeStamp);
                }
            }
        }
        Long earliestTime = Collections.min(firstTimeStamps);
        if (path.contains("receive")) {
            System.out.println("接收日志的最早时间：" + earliestTime);
        } else if (path.contains("process")) {
            System.out.println("处理日志的最早时间：" + earliestTime);
        }
        return  earliestTime;
    }

    /**
     * 获取日志中的最晚时间（0000.log的最后一行）
     *
     * @param path 路径，例如/home/test/data/receive/
     */
    private static Long GetLastEvent(String path) {
        MergeUtil mergeUtil = new MergeUtil();
        File file = new File(path);
        File[] files = file.listFiles();
        List<Long> zeroTimeStamps = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            // receiveFiles[i]: receive-0
            if (files[i].isDirectory()) {
                File[] logFiles = files[i].listFiles();
                Map<Integer, String> logNameMap = new HashMap<>();
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        String logName = logFile.getName().replace(".log", "");
                        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                        //判断文件名是否是整数，并排除读到error/error.log的可能性
                        if (pattern.matcher(logName).matches() && !logName.contains("error")) {
                            int logNameInt = Integer.parseInt(logName);
                            logNameMap.put(logNameInt, logFile.toString());
                        }
                    }

                    //获取0000.log的最后一条（日志的最晚时间）
                    String zeroLogPath = logNameMap.get(0000000000000000000);
//                    System.out.println(zeroLogPath);

                    List<String> zeroLogContent = mergeUtil.getAllContentFromFile(zeroLogPath);
                    LogEvent zeroLogEvent = JSONHelper.toObject(zeroLogContent.get(zeroLogContent.size()-1), LogEvent.class);
                    Long zeroLastTimeStamp = zeroLogEvent.getTimeStamp();
                    zeroTimeStamps.add(zeroLastTimeStamp);
//                    System.out.println(zeroLastTimeStamp);
                }
            }
        }
        Long lastZeroTime = Collections.max(zeroTimeStamps);
        if (path.contains("receive")) {
            System.out.println("接收日志的最晚时间：" + lastZeroTime);
        } else if (path.contains("process")) {
            System.out.println("处理日志的最晚时间：" + lastZeroTime);
        }
        return lastZeroTime;
    }
}