package com.hzgc.ftpserver;

import com.hzgc.ftpserver.util.FtpUtils;
import com.hzgc.util.common.FileUtil;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

public abstract class ClusterOverFtp implements Serializable {
    protected static Logger log = Logger.getLogger(ClusterOverFtp.class);
    protected static int listenerPort = 0;
    protected static String passivePorts = null;
    protected static DataConnectionConfigurationFactory dataConnConf;

    public void loadConfig() throws Exception {
        Properties props = new Properties();
        dataConnConf = new DataConnectionConfigurationFactory();
        props.load(new FileInputStream(FileUtil.loadResourceFile("cluster-over-ftp.properties")));
        log.info("Load configuration for ftp server from ./conf/cluster-over-ftp.properties");

        try {
            listenerPort = Integer.parseInt(props.getProperty("listener-port"));
            boolean checkPort = FtpUtils.checkPort(listenerPort);
            if (!checkPort) {
                log.error("The port settings for listener port is illegal and must be greater than 1024");
                System.exit(1);
            }
            log.info("The listener port:" + listenerPort + " for com.hzgc.ftpserver is already set");
        } catch (Exception e) {
            log.error("The port for listener is not set, Check that the \"listener-port\" is set", e);
            System.exit(1);
        }

        if (listenerPort != 0) {
            passivePorts = props.getProperty("data-ports");
            if (passivePorts == null) {
                log.info("The data ports is not set, use any available port");
            } else {
                dataConnConf.setPassivePorts(passivePorts);
                log.warn("The data ports is set:" + passivePorts);
            }
        }
    }

    public abstract void startFtpServer();
}
