package com.hzgc.service.putdata;

import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GenerateImportFile {
    private static List<String> importList = new ArrayList<>();
    private  static String sepator = "ZHONGXIAN";
    static {
        NativeFunction.init();
    }
    private static void findFile(String path, String addStr) {
        File filePath = new File(path);
        if (!filePath.exists()) {
            System.out.println("Current path is not exists, please check!");
            System.exit(1);
        }
        File[] fileList = filePath.listFiles();
        if (fileList != null) {
            for (File file: fileList) {
                if (file.isDirectory()) {
                  findFile(file.getAbsolutePath(), addStr);
                } else {
                    System.out.println(file.getAbsolutePath());
                    float[] temp = FaceFunction.featureExtract(file.getAbsolutePath()).getFeature();
                    if (temp != null && temp.length == 512) {
                        importList.add(file.getAbsolutePath() +
                                sepator +
                                addStr +
                                sepator +
                                FaceFunction.floatArray2string(temp));
                    } else {
                        importList.add(file.getAbsolutePath() +
                                sepator +
                                addStr +
                                sepator +
                                "null");
                    }
                }
            }
        }
    }

    private static void writeToFile(List<String> list, String outPut) throws Exception {
        for (String str: list) {
            File file = new File(outPut);
            FileOutputStream fos = new FileOutputStream(file, true);
            PrintStream ps = new PrintStream(fos);
            ps.println(str);
            ps.close();
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.out.println("[Parameter error] Please set the correct parameters for example \n" +
                    "com.hzgc.hbase.putdata.GenerateImportFile [directory absolute path] [objectType] [platID] [ouput path]");
            System.exit(1);
        }
        String path = args[0];
        String addStr = args[2] + sepator + args[1];
        String outPut = args[3];
        findFile(path, addStr);
        writeToFile(importList, outPut);
    }
}
