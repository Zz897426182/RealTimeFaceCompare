package com.hzgc.collect.expand.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * 从配置文件cluster-over-ftp.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class ClusterOverFtpProperHelper extends ProperHelper implements Serializable {
    private static Logger LOG
            = Logger.getLogger(ClusterOverFtpProperHelper.class);
    private static Properties props = new Properties();

    private static String logSize;
    private static String receiveQueueCapacity;
    private static String receiveLogDir;
    private static String processLogDir;
    private static String successLogDir;
    private static String mergeLogDir;
    private static String receiveNumber;
    private static String mergeScanTime;
    private static String faceDetectorNumber;
    private static String port;
    private static String dataPorts;
    private static String implicitSsl;
    private static String zookeeperAddress;
    private static String ftpSwitch;

    static {
        String properName = "cluster-over-ftp.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
                LOG.info("Load configuration for ftp server from ./conf/cluster-over-ftp.properties");

                setLogSize();
                setReceiveQueueCapacity();
                setReceiveLogDir();
                setProcessLogDir();
                setSuccessLogDir();
                setMergeLogDir();
                setReceiveNumber();
                setMergeScanTime();
                setFaceDetectorNumber();
                setPort();
                setDataPorts();
                setImplicitSsl();
                setZookeeperAddress();
                setFtpSwitch();
            } else {
                LOG.error("The property file " + properName + "doesn't exist!");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Catch an unknown error, can't load the configuration file" + properName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * set方法。验证配置文件中的值是否为符合条件的格式。
     */

    private static void setPort() {
        port = verifyPort("listener-port", "2121", props, LOG);
    }

    private static void setDataPorts() {
        dataPorts = verifyCommonValue("data-ports", "2223-2225", props, LOG);
    }

    private static void setImplicitSsl() {
        implicitSsl = verifyBooleanValue("implicitSsl", "false", props, LOG);
    }

    private static void setReceiveQueueCapacity() {
        receiveQueueCapacity = verifyPositiveIntegerValue("receive.queue.capacity", String.valueOf(Integer.MAX_VALUE), props, LOG);
    }

    private static void setReceiveLogDir() {
        receiveLogDir = verifyCommonValue("receive.log.dir", "/opt/RealTimeFaceCompare/ftp/data/receive", props, LOG);
    }

    private static void setProcessLogDir() {
        processLogDir = verifyCommonValue("process.log.dir", "/opt/RealTimeFaceCompare/ftp/data/process", props, LOG);
    }

    private static void setSuccessLogDir() {
        successLogDir = verifyCommonValue("success.log.dir", "/opt/RealTimeFaceCompare/ftp/success", props, LOG);
    }

    private static void setMergeLogDir() {
        mergeLogDir = verifyCommonValue("merge.log.dir", "/opt/RealTimeFaceCompare/ftp/merge", props, LOG);
    }

    private static void setMergeScanTime() {
        mergeScanTime = verifyPositiveIntegerValue("merge.scan.time", "", props, LOG);
    }

    private static void setReceiveNumber() {
        receiveNumber = verifyCommonValue("receive.number", "6", props, LOG);
    }

    private static void setLogSize() {
        logSize = verifyPositiveIntegerValue("log.Size", "300000", props, LOG);
    }

    private static void setFaceDetectorNumber() {
        faceDetectorNumber = verifyPositiveIntegerValue("face.detector.number", "", props, LOG);
    }

    private static void setZookeeperAddress() {
        zookeeperAddress = verifyIpPlusPortList("zookeeperAddress", props, LOG);
    }

    public static void setFtpSwitch() {
        ftpSwitch = verifyBooleanValue("ftp-switch", "true", props, LOG);
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public static Integer getPort() {
        return Integer.valueOf(port);
    }

    public static String getDataPorts() {
        return dataPorts;
    }

    public static Boolean getImplicitSsl() {
        return Boolean.valueOf(implicitSsl);
    }

    public static Integer getReceiveQueueCapacity() {
        return Integer.valueOf(receiveQueueCapacity);
    }

    public static String getReceiveLogDir() {
        return receiveLogDir;
    }

    public static String getProcessLogDir() {
        return processLogDir;
    }

    public static Integer getReceiveNumber() {
        return Integer.valueOf(receiveNumber);
    }

    public static String getSuccessLogDir() {
        return successLogDir;
    }

    public static String getMergeLogDir() {
        return mergeLogDir;
    }

    public static Integer getMergeScanTime() {
        return Integer.valueOf(mergeScanTime);
    }

    public static Integer getLogSize() {
        return Integer.valueOf(logSize);
    }

    public static Integer getFaceDetectorNumber() {
        return Integer.valueOf(faceDetectorNumber);
    }

    public static String getZookeeperAddress() {
        return zookeeperAddress;
    }

    public static String getFtpSwitch() {
        return ftpSwitch;
    }

    /**
     * 获取Properties属性的资源文件变量
     */
    public static Properties getProps() {
        return props;
    }
}
