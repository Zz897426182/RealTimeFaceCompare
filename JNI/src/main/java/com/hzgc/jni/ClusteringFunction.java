package com.hzgc.jni;

import java.io.Serializable;


public class ClusteringFunction implements Serializable {
    /**
     * æ€¿‡º∆À„
     *
     * @param features features array
     * @param fileName file name year-month.txt(2018-02.txt)
     * @param filePath absolute path of the results file
     * @return 0 or 1£¨1 success£¨0 fail
     */
    public static native int clusteringComputer(float[] features, String fileName, String filePath);

    static {
        System.loadLibrary("ClusteringLib");
    }
}
