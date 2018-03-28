package com.hzgc.ftpserver.util;

import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggerConfig {
    private static Logger LOG = Logger.getLogger(LoggerConfig.class);

    static {
        PropertyConfigurator.configureAndWatch(
                FileUtil.loadResourceFile("log4j.properties").getAbsolutePath(), 10000);
        LOG.info("Dynamic log configuration is successful! Log configuration file refresh time:" + 10000 + "ms");
    }
}
