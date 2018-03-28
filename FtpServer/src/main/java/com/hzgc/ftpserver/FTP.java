package com.hzgc.ftpserver;

import com.hzgc.ftpserver.util.LoggerConfig;
import com.hzgc.ftpserver.queue.DataProcess;
import com.hzgc.util.common.FileUtil;
import com.hzgc.ftpserver.command.CommandFactoryFactory;
import com.hzgc.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import com.hzgc.ftpserver.ftplet.FtpException;
import com.hzgc.ftpserver.listener.ListenerFactory;
import com.hzgc.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class FTP extends ClusterOverFtp {
    private static Logger log = Logger.getLogger(FTP.class);

    private static Map<Integer, Integer> pidMap = new HashMap<>();

    /*
      Set the dynamic log configuration file refresh time
     */
    static {
        new LoggerConfig();
    }

    @Override
    public void startFtpServer() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        log.info("Create " + FtpServerFactory.class + " successful");
        ListenerFactory listenerFactory = new ListenerFactory();
        log.info("Create " + ListenerFactory.class + " successful");
        //set the port of the listener
        listenerFactory.setPort(listenerPort);
        log.info("The port for listener is " + listenerPort);
        // replace the default listener
        serverFactory.addListener("default", listenerFactory.createListener());
        log.info("Add listner, name:default, class:" + serverFactory.getListener("default").getClass());
        // set customer user manager
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        try {
            userManagerFactory.setFile(FileUtil.loadResourceFile("users.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        serverFactory.setUserManager(userManagerFactory.createUserManager());
        log.info("Set customer user manager factory is successful, " + userManagerFactory.getClass());
        //set customer cmd factory
        CommandFactoryFactory commandFactoryFactory = new CommandFactoryFactory();
        serverFactory.setCommandFactory(commandFactoryFactory.createCommandFactory());
        log.info("Set customer command factory is successful, " + commandFactoryFactory.getClass());
        //set local file system
        NativeFileSystemFactory nativeFileSystemFactory = new NativeFileSystemFactory();
        serverFactory.setFileSystem(nativeFileSystemFactory);
        log.info("Set customer file system factory is successful, " + nativeFileSystemFactory.getClass());
        // TODO: 2017-10-9
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        log.info("FTP Server Maximum logon number:" + connectionConfigFactory.createUDConnectionConfig().getMaxLogins());
        serverFactory.setConnectionConfig(connectionConfigFactory.createUDConnectionConfig());
        log.info("Set user defined connection config file is successful, " + connectionConfigFactory.getClass());
        FtpServer server = serverFactory.createServer();
        try {
            server.start();
            Integer ftpPID = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
            pidMap.put(ftpPID, listenerPort);
        } catch (FtpException e) {
            e.printStackTrace();
        }

    }

    public static Map<Integer, Integer> getPidMap() {
        return pidMap;
    }

    public static void main(String args[]) throws Exception {
        FTP ftp = new FTP();
        ftp.loadConfig();
        DataProcess data = new DataProcess();
        data.reader();
        ftp.startFtpServer();
    }
}
