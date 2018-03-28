package com.hzgc.ftpserver.util;

import com.hzgc.util.common.FileUtil;
import com.hzgc.util.common.IOUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.SocketException;
import java.util.Properties;


/**
 * ftpClient文件下载
 */
public class DownloadUtils {

    private static final Logger LOG = Logger.getLogger(DownloadUtils.class);

    /**
     * 获取FTPClient对象
     *
     * @param ftpHost     FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort     FTP端口 默认为21
     * @return FTPClient
     */
    private static FTPClient getFTPClient(String ftpHost, String ftpUserName,
                                          String ftpPassword, int ftpPort) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX); //设置linux环境
            ftpClient.configure(conf);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) { //判断是否连接成功
                LOG.info("Failed to connect to FTPClient, user name or password error.");
                ftpClient.disconnect();
            } else {
                LOG.info("FTPClient connection successful.");
            }
        } catch (SocketException e) {
            LOG.info("FTP IP address may be incorrect, please configure correctly.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.info("FTP port error, please configure correctly.");
            e.printStackTrace();
        }
        return ftpClient;
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpHost       FTP的IP地址
     * @param ftpUserName   FTP用户名
     * @param ftpPassword   FTP用户密码
     * @param ftpPort       FTP端口号
     * @param ftpFilePath   FTP服务器中文件所在路径 格式： /3B0383FPAG00883/16/00
     * @param ftpFileName   从FTP服务器中下载的文件名称
     * @param localPath     下载到本地的位置 格式：D:/download
     * @param localFileName 下载到本地的文件名称
     */
    private static void downloadFtpFile(String ftpHost, String ftpUserName,
                                        String ftpPassword, int ftpPort,
                                        String ftpFilePath, String ftpFileName,
                                        String localPath, String localFileName) {

        FTPClient ftpClient;

        try {
            //连接FTPClient并转移到FTP服务器目录
            ftpClient = getFTPClient(ftpHost, ftpUserName, ftpPassword, ftpPort);
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpFilePath);//转移到FTP服务器目录

            //判断文件目录是否存在
            File dir = new File(localPath);
            if (!dir.exists()) {
                boolean temp = dir.mkdirs();
                if (temp) {
                    LOG.info("Create directory " + localPath + " successful");
                } else {
                    LOG.info("Create directory " + localPath + " failed");
                }
            }

            File localFile = new File(localPath + File.separatorChar + localFileName);
            OutputStream os = new FileOutputStream(localFile);
            //通过FTPClient获取文件
            ftpClient.retrieveFile(ftpFileName, os);

            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            LOG.error("Failed to find the " + ftpFilePath + " file below");
            e.printStackTrace();
        } catch (SocketException e) {
            LOG.error("Failed to connect FTPClient.");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("File read error.");
            e.printStackTrace();
        }
    }

    /**
     * 从FTP服务器下载文件
     *
     * @param ftpUrl        FTP地址
     * @param localPath     下载到本地的位置 格式：D:/download
     * @param localFileName 下载到本地的文件名称
     */
    public static void downloadFtpFile(String ftpUrl, String localPath, String localFileName) {
        if (ftpUrl != null && ftpUrl.length() > 0 &&
                localPath != null && localPath.length() > 0 &&
                localFileName != null && localFileName.length() > 0) {
            //解析FTP地址，得到ftpAddress、ftpPort、ftpFilePath、ftpFileName
            String ftpAddress = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
            String path = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
            int ftpPort = Integer.parseInt(path.substring(0, path.indexOf("/")));
            String ftpFilePath = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            String ftpFileName = path.substring(path.lastIndexOf("/") + 1);

            //通过ftpAddress.properties配置文件，ftpUserName、ftpPassword
            String ftpUserName = "";
            String ftpPassword = "";
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
                properties.load(inputStream);
                ftpUserName = properties.getProperty("user");
                ftpPassword = properties.getProperty("password");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeStream(inputStream);
            }

            downloadFtpFile(ftpAddress, ftpUserName, ftpPassword, ftpPort, ftpFilePath, ftpFileName, localPath, localFileName);
        }
    }

    /**
     * 从FTP服务器下载文件并转为字节数组
     *
     * @param ftpUrl FTP地址
     * @return 文件的字节数组
     */
    public static byte[] downloadftpFile2Bytes(String ftpUrl) {
        byte[] ftpFileBytes = null;
        if (ftpUrl != null && ftpUrl.length() > 0) {
            //解析FTP地址，得到ftpAddress、ftpPort、ftpFilePath、ftpFileName
            String ftpAddress = ftpUrl.substring(ftpUrl.indexOf("/") + 2, ftpUrl.lastIndexOf(":"));
            String path = ftpUrl.substring(ftpUrl.lastIndexOf(":") + 1);
            int ftpPort = Integer.parseInt(path.substring(0, path.indexOf("/")));
            String ftpFilePath = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            String ftpFileName = path.substring(path.lastIndexOf("/") + 1);

            //通过ftpAddress.properties配置文件，ftpUserName、ftpPassword
            String ftpUserName = "";
            String ftpPassword = "";
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties")));
                properties.load(inputStream);
                ftpUserName = properties.getProperty("user");
                ftpPassword = properties.getProperty("password");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtil.closeStream(inputStream);
            }

            FTPClient ftpClient;
            InputStream in;

            try {
                //连接FTPClient并转移到FTP服务器目录
                ftpClient = getFTPClient(ftpAddress, ftpUserName, ftpPassword, ftpPort);
                ftpClient.setControlEncoding("UTF-8"); // 中文支持
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.changeWorkingDirectory(ftpFilePath);//转移到FTP服务器目录

                //通过FTPClient获取文件输入流并转为byte[]
                in = ftpClient.retrieveFileStream(ftpFileName);
                ftpFileBytes = FtpUtils.inputStreamCacher(in).toByteArray();

                in.close();
                ftpClient.logout();

            } catch (FileNotFoundException e) {
                LOG.error("Failed to find the " + ftpFilePath + " file below");
                e.printStackTrace();
            } catch (SocketException e) {
                LOG.error("Failed to connect FTPClient.");
                e.printStackTrace();
            } catch (IOException e) {
                LOG.error("File read error.");
                e.printStackTrace();
            }
        } else {
            LOG.warn("method param is error.");
        }
        return ftpFileBytes;
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param host     FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器基础目录
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/2015/01/01。文件的路径为basePath+filePath
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    private static boolean uploadFile(String host, int port, String username, String password, String basePath,
                                      String filePath, String filename, InputStream input) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(basePath + filePath)) {
                //如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) {
                        continue;
                    }
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {
                        if (!ftp.makeDirectory(tempPath)) {
                            return false;
                        } else {
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //上传文件
            if (!ftp.storeFile(filename, input)) {
                return false;
            }
            input.close();
            ftp.logout();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
        }
        return result;
    }

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param url      FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false *
     */
    public static boolean uploadFileNew(String url,// FTP服务器hostname
                                        int port,// FTP服务器端口
                                        String username, // FTP登录账号
                                        String password, // FTP登录密码
                                        String path, // FTP服务器保存目录
                                        String filename, // 上传到FTP服务器上的文件名
                                        InputStream input // 输入流
    ) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("UTF-8");
        try {
            int reply;
            ftp.connect(url, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            // 登录
            ftp.login(username, password);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return false;
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.makeDirectory(path);
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ignored) {
                }
            }
        }
        return success;
    }

    /**
     * 将本地文件上传到FTP服务器上 *
     */
    public static void upLoadFromProduction(String hostname,// FTP服务器hostname
                                            int port,// FTP服务器端口
                                            String username, // FTP登录账号
                                            String password, // FTP登录密码
                                            String basepath, // FTP服务器保存目录
                                            String filepath, // 文件存储路劲
                                            String filename, //上传到FTP服务器上的文件名
                                            String orginfilename //输入流文件名
    ) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(orginfilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        boolean flag = uploadFile(hostname, port, username, password, basepath, filepath, filename, in);
            if (flag) {
                LOG.info("upload file successfule");
            } else {
                LOG.error("upload file successfule");
            }

    }
}