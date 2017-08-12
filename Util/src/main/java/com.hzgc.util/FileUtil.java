package com.hzgc.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.net.URL;

public class FileUtil {
    private static Logger LOG = Logger.getLogger(FileUtil.class);

    public static File loadResourceFile(String resourceName) {
        if (false) {
            URL resource = FileUtil.class.getResource("/");
            String confPath = resource.getPath();
            confPath = confPath.substring(5, confPath.lastIndexOf("/lib"));
            confPath = confPath + "/conf/";
            File sourceFile = new File(confPath + resourceName);
            PropertyConfigurator.configure(confPath + "/com/hzgc/com.hzgc.ftpserver/log4j.properties");
            PropertyConfigurator.configure(confPath + "/hbase-site.xml");
            if (!sourceFile.exists()) {
                LOG.error("The local resource file:" + new File(confPath).getAbsolutePath()
                        + "/" + resourceName + " is not found, " +
                        "please check it, System exit.");
                System.exit(1);
            }
            LOG.info("The resource file:" + new File(confPath).getAbsolutePath() + "was load successfull");
            return sourceFile;
        } else {
            URL resource = FileUtil.class.getResource("/" + resourceName);
            if (resource != null) {
                return new File(resource.getFile());
            }
        }
        LOG.error("Can not find rsource file:" + FileUtil.class.getResource("/") + resourceName);
        return null;
    }
}
