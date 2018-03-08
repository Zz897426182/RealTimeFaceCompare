package com.hzgc.collect;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 从配置文件ftpAddress.properties中：
 * 验证其中的配置；读取所需的配置。（马燊偲）
 */
public class UpDataToFtpProperHelper {

    private static Logger LOG = Logger.getLogger(UpDataToFtpProperHelper.class);
    private static Properties props = new Properties();

    int loopNum;
    int threadNum;
    int port;
    String ip;

    static {
        String properName = "ftpAddress.properties";
        FileInputStream in = null;
        try {
            File file = FileUtil.loadResourceFile(properName);
            if (file != null) {
                in = new FileInputStream(file);
                props.load(in);
            } else {
                LOG.error("The property file " + properName + "doesn't exist!");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Catch an unknown error, can't load the configuration file" + properName);
        } finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * get方法。提供获取配置文件中的值的方法。
     */

    public int getLoopNum() {
        loopNum = Integer.parseInt(props.getProperty("loopNum"));
        return loopNum;
    }

    public int getThreadNum() {
        threadNum = Integer.parseInt(props.getProperty("threadNum"));
        return threadNum;
    }

    public int getPort() {
        port = Integer.parseInt(props.getProperty("port"));
        return port;
    }

    public String getIp() {
        ip = props.getProperty("ip");
        return ip;
    }
}
